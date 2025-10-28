package com.estapar.parking.controller;

import com.estapar.parking.dto.GarageConfigDTO;
import com.estapar.parking.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/garage")
public class GarageController {
    
    @Autowired
    private ParkingService parkingService;
    
    @GetMapping
    public ResponseEntity<GarageConfigDTO> getGarageConfiguration() {
        System.out.println("🏢 GET /garage - Fetching garage configuration");
        
        GarageConfigDTO config = parkingService.getGarageConfiguration();
        
        return ResponseEntity.ok(config);
    }
}
