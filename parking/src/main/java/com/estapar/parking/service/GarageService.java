package com.estapar.parking.service;

import com.estapar.parking.dto.GarageConfigDTO;
import com.estapar.parking.dto.SectorConfigDTO;
import com.estapar.parking.dto.SpotConfigDTO;
import com.estapar.parking.model.Sector;
import com.estapar.parking.model.Spot;
import com.estapar.parking.repository.SectorRepository;
import com.estapar.parking.repository.SpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class GarageService {
    
    @Autowired
    private SectorRepository sectorRepository;
    
    @Autowired
    private SpotRepository spotRepository;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    // ✅ Use o mock local
    private static final String SIMULATOR_URL = "http://localhost:3003/mock/garage";
    
//    @Transactional
//    public void initializeGarage() {
//        try {
//            System.out.println("🚀 Fetching garage configuration from: " + SIMULATOR_URL);
//            
//            GarageConfigDTO config = restTemplate.getForObject(SIMULATOR_URL, GarageConfigDTO.class);
//            
//            if (config == null) {
//                throw new RuntimeException("Failed to fetch garage configuration");
//            }
//            
//            System.out.println("📦 Received configuration with " + 
//                (config.getGarage() != null ? config.getGarage().size() : 0) + " sectors");
//            
//            // Criar mapa de setores para lookup rápido
//            Map<String, Sector> sectorMap = new HashMap<>();
//            
//            // Salvar setores
//            if (config.getGarage() != null) {
//                for (SectorConfigDTO sectorConfig : config.getGarage()) {
//                    Sector sector = new Sector(
//                        sectorConfig.getSector(),
//                        sectorConfig.getBasePrice(),
//                        sectorConfig.getMaxCapacity()
//                    );
//                    Sector savedSector = sectorRepository.save(sector);
//                    sectorMap.put(savedSector.getName(), savedSector);
//                    System.out.println("✅ Created sector: " + savedSector.getName() + 
//                        " (capacity: " + savedSector.getMaxCapacity() + ")");
//                }
//            }
//            
//            // Salvar vagas
//            if (config.getSpots() != null) {
//                int spotCount = 0;
//                for (SpotConfigDTO spotConfig : config.getSpots()) {
//                    Sector sector = sectorMap.get(spotConfig.getSector());
//                    if (sector != null) {
//						Spot spot = new Spot(spotConfig.getId(), spotConfig.getLng(), spotConfig.getLat(), sector);
//                        spotRepository.save(spot);
//                        spotCount++;
//                    }
//                }
//                System.out.println("✅ Created " + spotCount + " spots");
//            }
//            
//            System.out.println("🎉 Garage initialized successfully!");
//            
//        } catch (Exception e) {
//            System.err.println("❌ Error initializing garage: " + e.getMessage());
//            e.printStackTrace();
//            throw new RuntimeException("Failed to initialize garage", e);
//        }
//    }
}