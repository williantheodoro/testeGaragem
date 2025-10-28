package com.estapar.parking.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingServiceTest {
    
    @Mock
    private ParkingSessionRepository sessionRepository;
    
    @Mock
    private SectorRepository sectorRepository;
    
    @Mock
    private SpotRepository spotRepository;
    
    @InjectMocks
    private ParkingService parkingService;
    
    private Sector sector;
    private Spot spot;
    private WebhookEventDTO entryEvent;
    
    @BeforeEach
    void setUp() {
        // ✅ Criar setor
        sector = new Sector("A", new BigDecimal("10.00"), 100);
        sector.setId(1L);
        
        // ✅ Criar vaga com ID String (ex: "A1")
        spot = new Spot("A1", -46.655981, -23.561684, sector);
        
        // ✅ Criar evento de entrada
        entryEvent = new WebhookEventDTO();
        entryEvent.setEventType("ENTRY");
        entryEvent.setLicensePlate("ABC1234");
        entryEvent.setEntryTime(LocalDateTime.now());
    }
    
    @Test
    void testHandleEntrySuccess() {
        // Arrange
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(anyString()))
            .thenReturn(Optional.empty());
        
        List<Sector> sectors = new ArrayList<>();
        sectors.add(sector);
        when(sectorRepository.findAll()).thenReturn(sectors);
        
        when(spotRepository.countBySectorAndOccupied(any(), eq(true)))
            .thenReturn(50L); // 50% ocupado
        
        ParkingSession mockSession = new ParkingSession(
            "ABC1234", sector, LocalDateTime.now(), new BigDecimal("10.00")
        );
        mockSession.setId(1L);
        when(sessionRepository.save(any())).thenReturn(mockSession);
        
        // Act
        ParkingSession result = parkingService.handleEntry(entryEvent);
        
        // Assert
        assertNotNull(result);
        verify(sessionRepository, times(1)).save(any(ParkingSession.class));
    }
    
    @Test
    void testHandleEntryVehicleAlreadyParked() {
        // Arrange
        ParkingSession existingSession = new ParkingSession(
            "ABC1234", sector, LocalDateTime.now(), new BigDecimal("10.00")
        );
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(anyString()))
            .thenReturn(Optional.of(existingSession));
        
        // Act & Assert
        VehicleAlreadyParkedException exception = assertThrows(
            VehicleAlreadyParkedException.class, 
            () -> parkingService.handleEntry(entryEvent)
        );
        
        assertTrue(exception.getMessage().contains("ABC1234"));
        verify(sessionRepository, never()).save(any());
    }
    
    @Test
    void testHandleEntryParkingFull() {
        // Arrange
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(anyString()))
            .thenReturn(Optional.empty());
        
        List<Sector> sectors = new ArrayList<>();
        sectors.add(sector);
        when(sectorRepository.findAll()).thenReturn(sectors);
        
        // Setor completamente cheio
        when(spotRepository.countBySectorAndOccupied(any(), eq(true)))
            .thenReturn(100L);
        
        // Act & Assert
        assertThrows(ParkingFullException.class, 
            () -> parkingService.handleEntry(entryEvent));
        
        verify(sessionRepository, never()).save(any());
    }
    
    @Test
    void testHandleEntryDynamicPricingLowOccupancy() {
        // Arrange - Ocupação < 25% = desconto de 10%
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(anyString()))
            .thenReturn(Optional.empty());
        
        List<Sector> sectors = new ArrayList<>();
        sectors.add(sector);
        when(sectorRepository.findAll()).thenReturn(sectors);
        
        when(spotRepository.countBySectorAndOccupied(any(), eq(true)))
            .thenReturn(20L); // 20% ocupado
        
        ParkingSession savedSession = new ParkingSession(
            "ABC1234", sector, LocalDateTime.now(), new BigDecimal("9.00") // 10.00 * 0.90
        );
        when(sessionRepository.save(any())).thenReturn(savedSession);
        
        // Act
        ParkingSession result = parkingService.handleEntry(entryEvent);
        
        // Assert
        assertNotNull(result);
        verify(sessionRepository, times(1)).save(any());
    }
    
    @Test
    void testHandleParkedSuccess() {
        // Arrange
        ParkingSession session = new ParkingSession(
            "ABC1234", sector, LocalDateTime.now(), new BigDecimal("10.00")
        );
        session.setId(1L);
        
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(anyString()))
            .thenReturn(Optional.of(session));
        
        List<Spot> spots = new ArrayList<>();
        spots.add(spot);
        when(spotRepository.findBySectorAndOccupied(any(), eq(false)))
            .thenReturn(spots);
        
        when(sessionRepository.save(any())).thenReturn(session);
        when(spotRepository.save(any())).thenReturn(spot);
        
        WebhookEventDTO parkedEvent = new WebhookEventDTO();
        parkedEvent.setEventType("PARKED");
        parkedEvent.setLicensePlate("ABC1234");
        parkedEvent.setLat(new BigDecimal("-23.561684"));
        parkedEvent.setLng(new BigDecimal("-46.655981"));
        
        // Act
        ParkingSession result = parkingService.handleParked(parkedEvent);
        
        // Assert
        assertNotNull(result);
        verify(spotRepository, times(1)).save(any(Spot.class));
        verify(sessionRepository, times(1)).save(any(ParkingSession.class));
    }
    
    @Test
    void testHandleParkedWithoutCoordinates() {
        // Arrange
        ParkingSession session = new ParkingSession(
            "ABC1234", sector, LocalDateTime.now(), new BigDecimal("10.00")
        );
        
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(anyString()))
            .thenReturn(Optional.of(session));
        
        List<Spot> spots = new ArrayList<>();
        spots.add(spot);
        when(spotRepository.findBySectorAndOccupied(any(), eq(false)))
            .thenReturn(spots);
        
        when(sessionRepository.save(any())).thenReturn(session);
        when(spotRepository.save(any())).thenReturn(spot);
        
        WebhookEventDTO parkedEvent = new WebhookEventDTO();
        parkedEvent.setEventType("PARKED");
        parkedEvent.setLicensePlate("ABC1234");
        // Sem coordenadas
        
        // Act
        ParkingSession result = parkingService.handleParked(parkedEvent);
        
        // Assert
        assertNotNull(result);
        verify(spotRepository, times(1)).save(any());
    }
    
    @Test
    void testHandleParkedVehicleNotFound() {
        // Arrange
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(anyString()))
            .thenReturn(Optional.empty());
        
        WebhookEventDTO parkedEvent = new WebhookEventDTO();
        parkedEvent.setEventType("PARKED");
        parkedEvent.setLicensePlate("ABC1234");
        
        // Act & Assert
        assertThrows(VehicleNotFoundException.class, 
            () -> parkingService.handleParked(parkedEvent));
        
        verify(spotRepository, never()).save(any());
    }
    
    @Test
    void testHandleParkedNoAvailableSpots() {
        // Arrange
        ParkingSession session = new ParkingSession(
            "ABC1234", sector, LocalDateTime.now(), new BigDecimal("10.00")
        );
        
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(anyString()))
            .thenReturn(Optional.of(session));
        
        // Sem vagas disponíveis
        when(spotRepository.findBySectorAndOccupied(any(), eq(false)))
            .thenReturn(new ArrayList<>());
        
        WebhookEventDTO parkedEvent = new WebhookEventDTO();
        parkedEvent.setEventType("PARKED");
        parkedEvent.setLicensePlate("ABC1234");
        
        // Act & Assert
        assertThrows(ParkingFullException.class, 
            () -> parkingService.handleParked(parkedEvent));
    }
    
    @Test
    void testHandleExitSuccess() {
        // Arrange
        LocalDateTime entryTime = LocalDateTime.now().minusHours(2);
        ParkingSession session = new ParkingSession(
            "ABC1234", sector, entryTime, new BigDecimal("10.00")
        );
        session.setId(1L);
        session.setSpot(spot);
        
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(anyString()))
            .thenReturn(Optional.of(session));
        
        when(sessionRepository.save(any())).thenReturn(session);
        when(spotRepository.save(any())).thenReturn(spot);
        
        WebhookEventDTO exitEvent = new WebhookEventDTO();
        exitEvent.setEventType("EXIT");
        exitEvent.setLicensePlate("ABC1234");
        exitEvent.setExitTime(LocalDateTime.now());
        
        // Act
        ParkingSession result = parkingService.handleExit(exitEvent);
        
        // Assert
        assertNotNull(result);
        verify(spotRepository, times(1)).save(any(Spot.class));
        verify(sessionRepository, times(1)).save(any(ParkingSession.class));
    }
    
    @Test
    void testHandleExitFreeParking() {
        // Arrange - menos de 30 minutos
        LocalDateTime now = LocalDateTime.now();
        ParkingSession session = new ParkingSession(
            "ABC1234", sector, now.minusMinutes(20), new BigDecimal("10.00")
        );
        session.setId(1L);
        session.setSpot(spot);
        
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(anyString()))
            .thenReturn(Optional.of(session));
        
        when(sessionRepository.save(any())).thenAnswer(invocation -> {
            ParkingSession saved = invocation.getArgument(0);
            // Verificar que o valor final é zero
            assertEquals(BigDecimal.ZERO, saved.getFinalAmount());
            return saved;
        });
        
        when(spotRepository.save(any())).thenReturn(spot);
        
        WebhookEventDTO exitEvent = new WebhookEventDTO();
        exitEvent.setEventType("EXIT");
        exitEvent.setLicensePlate("ABC1234");
        exitEvent.setExitTime(now);
        
        // Act
        ParkingSession result = parkingService.handleExit(exitEvent);
        
        // Assert
        assertNotNull(result);
        verify(sessionRepository, times(1)).save(any());
    }
    
    @Test
    void testHandleExitWithoutSpot() {
        // Arrange - sessão sem vaga atribuída
        LocalDateTime entryTime = LocalDateTime.now().minusHours(1);
        ParkingSession session = new ParkingSession(
            "ABC1234", sector, entryTime, new BigDecimal("10.00")
        );
        session.setId(1L);
        // session.setSpot(null); - sem vaga
        
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(anyString()))
            .thenReturn(Optional.of(session));
        
        when(sessionRepository.save(any())).thenReturn(session);
        
        WebhookEventDTO exitEvent = new WebhookEventDTO();
        exitEvent.setEventType("EXIT");
        exitEvent.setLicensePlate("ABC1234");
        exitEvent.setExitTime(LocalDateTime.now());
        
        // Act
        ParkingSession result = parkingService.handleExit(exitEvent);
        
        // Assert
        assertNotNull(result);
        verify(spotRepository, never()).save(any()); // Não deve tentar liberar vaga
        verify(sessionRepository, times(1)).save(any());
    }
    
    @Test
    void testHandleExitVehicleNotFound() {
        // Arrange
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(anyString()))
            .thenReturn(Optional.empty());
        
        WebhookEventDTO exitEvent = new WebhookEventDTO();
        exitEvent.setEventType("EXIT");
        exitEvent.setLicensePlate("ABC1234");
        exitEvent.setExitTime(LocalDateTime.now());
        
        // Act & Assert
        assertThrows(VehicleNotFoundException.class, 
            () -> parkingService.handleExit(exitEvent));
        
        verify(sessionRepository, never()).save(any());
    }
    
    @Test
    void testCalculateFinalAmountMultipleHours() {
        // Arrange - 3 horas e 15 minutos = 4 horas cobradas
        LocalDateTime entryTime = LocalDateTime.of(2025, 10, 27, 10, 0);
        LocalDateTime exitTime = LocalDateTime.of(2025, 10, 27, 13, 15);
        
        ParkingSession session = new ParkingSession(
            "ABC1234", sector, entryTime, new BigDecimal("10.00")
        );
        session.setSpot(spot);
        
        when(sessionRepository.findByLicensePlateAndExitTimeIsNull(anyString()))
            .thenReturn(Optional.of(session));
        
        when(sessionRepository.save(any())).thenAnswer(invocation -> {
            ParkingSession saved = invocation.getArgument(0);
            // 4 horas * R$ 10.00 = R$ 40.00
            assertEquals(new BigDecimal("40.00"), saved.getFinalAmount());
            return saved;
        });
        
        when(spotRepository.save(any())).thenReturn(spot);
        
        WebhookEventDTO exitEvent = new WebhookEventDTO();
        exitEvent.setEventType("EXIT");
        exitEvent.setLicensePlate("ABC1234");
        exitEvent.setExitTime(exitTime);
        
        // Act
        parkingService.handleExit(exitEvent);
        
        // Assert
        verify(sessionRepository, times(1)).save(any());
    }

    @Test
    void testGetParkingStatistics() {
    // Arrange
    List<Sector> sectors = new ArrayList<>();
    sectors.add(sector);
    when(sectorRepository.findAll()).thenReturn(sectors);
    
    when(spotRepository.countBySectorAndOccupied(sector, true))
        .thenReturn(50L);
    when(spotRepository.countBySectorAndOccupied(sector, false))
        .thenReturn(50L);
    
    // Act
    String stats = parkingService.getParkingStatistics();
    
    // Assert
    assertNotNull(stats, "Statistics should not be null");
    assertFalse(stats.isEmpty(), "Statistics should not be empty");
    
    // Verificações case-insensitive e flexíveis
    String statsUpper = stats.toUpperCase();
    
    assertTrue(statsUpper.contains("SECTOR") || statsUpper.contains("SETOR"), 
        "Should contain 'Sector' or 'Setor'");
    
    assertTrue(stats.contains("A"), 
        "Should contain sector name 'A'");
    
    assertTrue(stats.contains("50") && stats.contains("100"), 
        "Should contain '50' and '100'");
    
    // Verificar que tem algum formato de porcentagem (50.0% ou 50,0% ou 50%)
    assertTrue(stats.matches("(?s).*\\d+([.,]\\d+)?%.*"), 
        "Should contain percentage format");
    
    System.out.println("✅ Statistics output:");
    System.out.println(stats);
}


}