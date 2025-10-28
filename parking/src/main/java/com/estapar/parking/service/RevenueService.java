package com.estapar.parking.service;

import com.estapar.parking.model.Sector;
import com.estapar.parking.repository.ParkingSessionRepository;
import com.estapar.parking.repository.SectorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class RevenueService {
    
    @Autowired
    private ParkingSessionRepository sessionRepository;
    
    @Autowired
    private SectorRepository sectorRepository;
    
    public BigDecimal calculateRevenue(LocalDate date, String sectorName) {
        Sector sector = sectorRepository.findBysectorCode(sectorName)
            .orElseThrow(() -> new RuntimeException("Sector not found: " + sectorName));
        
        BigDecimal revenue = sessionRepository.sumRevenueByDateAndSector(date, sector);
        
        return revenue != null ? revenue : BigDecimal.ZERO;
    }
}
