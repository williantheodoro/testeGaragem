package com.estapar.parking.service;

import com.estapar.parking.dto.GarageConfigDTO;
import com.estapar.parking.dto.WebhookEventDTO;
import com.estapar.parking.exception.ParkingFullException;
import com.estapar.parking.exception.VehicleAlreadyParkedException;
import com.estapar.parking.exception.VehicleNotFoundException;
import com.estapar.parking.model.ParkingSession;
import com.estapar.parking.model.Sector;
import com.estapar.parking.model.Spot;
import com.estapar.parking.repository.ParkingSessionRepository;
import com.estapar.parking.repository.SectorRepository;
import com.estapar.parking.repository.SpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ParkingService {
    
    @Autowired
    private ParkingSessionRepository sessionRepository;
    
    @Autowired
    private SectorRepository sectorRepository;
    
    @Autowired
    private SpotRepository spotRepository;
    
    @Transactional
    public ParkingSession handleEntry(WebhookEventDTO event) {
        System.out.println("🚗 Processing ENTRY for vehicle: " + event.getLicensePlate());
        
        // Verificar se veículo já está estacionado
        Optional<ParkingSession> existingSession = sessionRepository
            .findByLicensePlateAndExitTimeIsNull(event.getLicensePlate());
        
        if (existingSession.isPresent()) {
            throw new VehicleAlreadyParkedException(
                "Vehicle " + event.getLicensePlate() + " is already parked"
            );
        }
        
        // Buscar primeiro setor disponível
        List<Sector> sectors = sectorRepository.findAll();
        Sector selectedSector = null;
        
        for (Sector sector : sectors) {
            Long occupiedCount = spotRepository.countBySectorAndOccupied(sector, true);
            if (occupiedCount < sector.getMaxCapacity()) {
                selectedSector = sector;
                System.out.println("✅ Selected sector: " + sector.getSectorCode() + 
                    " (" + occupiedCount + "/" + sector.getMaxCapacity() + " occupied)");
                break;
            }
        }
        
        if (selectedSector == null) {
            throw new ParkingFullException("Parking is full");
        }
        
        // Calcular preço dinâmico baseado na lotação
        Long occupiedCount = spotRepository.countBySectorAndOccupied(selectedSector, true);
        BigDecimal appliedPrice = calculateDynamicPrice(
            selectedSector.getBasePrice(),
            occupiedCount,
            selectedSector.getMaxCapacity()
        );
        
        System.out.println("💰 Applied price: R$ " + appliedPrice + 
            " (base: R$ " + selectedSector.getBasePrice() + ")");
        
        //  Criar sessão SEM spot (será atribuído no PARKED)
        ParkingSession session = new ParkingSession();
        session.setLicensePlate(event.getLicensePlate());
        session.setSector(selectedSector);
        session.setEntryTime(event.getEntryTime());
        session.setAppliedPrice(appliedPrice);
        // ✅ NÃO defini spot aqui - será definido no handleParked
        
        ParkingSession savedSession = sessionRepository.save(session);
        System.out.println("✅ Entry session created with ID: " + savedSession.getId());
        
        return savedSession;
    }
    
    @Transactional
    public ParkingSession handleParked(WebhookEventDTO event) {
        System.out.println("🅿️ Processing PARKED for vehicle: " + event.getLicensePlate());
        
        ParkingSession session = sessionRepository
            .findByLicensePlateAndExitTimeIsNull(event.getLicensePlate())
            .orElseThrow(() -> new VehicleNotFoundException(
                "No active session found for vehicle " + event.getLicensePlate()
            ));
        
        // Encontrar vaga mais próxima disponível
        List<Spot> availableSpots = spotRepository
            .findBySectorAndOccupied(session.getSector(), false);
        
        if (availableSpots.isEmpty()) {
            throw new ParkingFullException("No available spots in sector " + session.getSector().getSectorCode());
        }
        
        // Converter BigDecimal para Double
        Double targetLat = event.getLat() != null ? event.getLat().doubleValue() : null;
        Double targetLng = event.getLng() != null ? event.getLng().doubleValue() : null;
        
        Spot closestSpot = findClosestSpot(availableSpots, targetLat, targetLng);
        
        System.out.println("🎯 Assigned spot: " + closestSpot.getId() + 
            " at (" + closestSpot.getLatitude() + ", " + closestSpot.getLongitude() + ")");
        
        // Marcar vaga como ocupada
        closestSpot.setOccupied(true);
        closestSpot.setOccupiedAt(LocalDateTime.now());
        closestSpot.setOccupiedBy(event.getLicensePlate());
        spotRepository.save(closestSpot);
        
        //  Atualizar sessão com o spot
        session.setSpot(closestSpot);
        session.setParkedTime(LocalDateTime.now());
        ParkingSession updatedSession = sessionRepository.save(session);
        
        System.out.println("✅ Vehicle parked successfully");
        
        return updatedSession;
    }
    
    @Transactional
    public ParkingSession handleExit(WebhookEventDTO event) {
        System.out.println("🚪 Processing EXIT for vehicle: " + event.getLicensePlate());
        
        ParkingSession session = sessionRepository
            .findByLicensePlateAndExitTimeIsNull(event.getLicensePlate())
            .orElseThrow(() -> new VehicleNotFoundException(
                "No active session found for vehicle " + event.getLicensePlate()
            ));
        
        // Calcular valor final
        BigDecimal finalAmount = calculateFinalAmount(
            session.getEntryTime(),
            event.getExitTime(),
            session.getAppliedPrice()
        );
        
        System.out.println("💵 Final amount: R$ " + finalAmount);
        
        // Liberar vaga
        if (session.getSpot() != null) {
            Spot spot = session.getSpot();
            spot.setOccupied(false);
            spot.setOccupiedAt(null);
            spot.setOccupiedBy(null);
            spotRepository.save(spot);
            System.out.println("✅ Spot " + spot.getId() + " released");
        }
        
        // Atualizar sessão
        session.setExitTime(event.getExitTime());
        session.setFinalAmount(finalAmount);
        ParkingSession completedSession = sessionRepository.save(session);
        
        System.out.println("✅ Exit processed successfully");
        
        return completedSession;
    }
    
    private BigDecimal calculateDynamicPrice(BigDecimal basePrice, Long occupiedCount, Integer maxCapacity) {
        double occupancyRate = (double) occupiedCount / maxCapacity;
        BigDecimal multiplier;
        
        if (occupancyRate < 0.25) {
            multiplier = new BigDecimal("0.90"); // -10%
        } else if (occupancyRate < 0.50) {
            multiplier = BigDecimal.ONE; // 0%
        } else if (occupancyRate < 0.75) {
            multiplier = new BigDecimal("1.10"); // +10%
        } else {
            multiplier = new BigDecimal("1.25"); // +25%
        }
        
        return basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateFinalAmount(
        LocalDateTime entryTime,
        LocalDateTime exitTime,
        BigDecimal appliedPrice
    ) {
        Duration duration = Duration.between(entryTime, exitTime);
        long minutes = duration.toMinutes();
        
        System.out.println("⏱️ Parking duration: " + minutes + " minutes");
        
        // Primeiros 30 minutos grátis
        if (minutes <= 30) {
            System.out.println("🎁 Free parking (under 30 minutes)");
            return BigDecimal.ZERO;
        }
        
        // Calcular horas (arredondar para cima)
        double hours = minutes / 60.0;
        long ceiledHours = (long) Math.ceil(hours);
        
        System.out.println("📊 Charged hours: " + ceiledHours + " (actual: " + hours + ")");
        
        return appliedPrice.multiply(BigDecimal.valueOf(ceiledHours))
            .setScale(2, RoundingMode.HALF_UP);
    }
    
    private Spot findClosestSpot(List<Spot> spots, Double targetLat, Double targetLng) {
        if (targetLat == null || targetLng == null) {
            // Se não tiver coordenadas, retorna a primeira vaga disponível
            return spots.get(0);
        }
        
        Spot closestSpot = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Spot spot : spots) {
            double distance = calculateDistance(
                spot.getLatitude(),
                spot.getLongitude(),
                targetLat,
                targetLng
            );
            
            if (distance < minDistance) {
                minDistance = distance;
                closestSpot = spot;
            }
        }
        
        return closestSpot;
    }
    
    private double calculateDistance(
        Double lat1, Double lng1,
        Double lat2, Double lng2
    ) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return Double.MAX_VALUE;
        }
        
        double dLat = lat2 - lat1;
        double dLng = lng2 - lng1;
        return Math.sqrt(dLat * dLat + dLng * dLng);
    }
    
    public String getParkingStatistics() {
        List<Sector> sectors = sectorRepository.findAll();
        StringBuilder stats = new StringBuilder("📊 Parking Statistics:\n");
        
        for (Sector sector : sectors) {
            Long occupied = spotRepository.countBySectorAndOccupied(sector, true);
            Long available = spotRepository.countBySectorAndOccupied(sector, false);
            double occupancyRate = (double) occupied / sector.getMaxCapacity() * 100;
            
            stats.append(String.format("  Sector %s: %d/%d occupied (%.1f%%) - R$ %.2f/hour\n",
                sector.getSectorCode(),
                occupied,
                sector.getMaxCapacity(),
                occupancyRate,
                calculateDynamicPrice(sector.getBasePrice(), occupied, sector.getMaxCapacity())
            ));
        }
        
        return stats.toString();
    }
    
    public GarageConfigDTO getGarageConfiguration() {
        System.out.println("📋 Fetching garage configuration...");
        
        // Buscar todos os setores
        List<Sector> sectors = sectorRepository.findAll();
        List<GarageConfigDTO.SectorConfigDTO> sectorConfigs = new ArrayList<>();
        
        for (Sector sector : sectors) {
            GarageConfigDTO.SectorConfigDTO sectorConfig = new GarageConfigDTO.SectorConfigDTO(
                sector.getSectorCode(),
                sector.getBasePrice(),
                sector.getMaxCapacity()
            );
            sectorConfigs.add(sectorConfig);
        }
        
        // Buscar todas as vagas
        List<Spot> spots = spotRepository.findAll();
        List<GarageConfigDTO.SpotConfigDTO> spotConfigs = new ArrayList<>();
        
        for (Spot spot : spots) {
            GarageConfigDTO.SpotConfigDTO spotConfig = new GarageConfigDTO.SpotConfigDTO(
                spot.getId(),
                spot.getSector().getSectorCode(),
                spot.getLatitude(),
                spot.getLongitude()
            );
            spotConfigs.add(spotConfig);
        }
        
        System.out.println("✅ Configuration loaded: " + sectorConfigs.size() + 
            " sectors, " + spotConfigs.size() + " spots");
        
        return new GarageConfigDTO(sectorConfigs, spotConfigs);
    }
}
