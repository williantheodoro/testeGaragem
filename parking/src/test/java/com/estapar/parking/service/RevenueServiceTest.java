package com.estapar.parking.service;

import com.estapar.parking.model.Sector;
import com.estapar.parking.repository.ParkingSessionRepository;
import com.estapar.parking.repository.SectorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevenueServiceTest {
    
    @Mock
    private ParkingSessionRepository sessionRepository;
    
    @Mock
    private SectorRepository sectorRepository;
    
    @InjectMocks
    private RevenueService revenueService;
    
    private Sector sector;
    
    @BeforeEach
    void setUp() {
        sector = new Sector("A", new BigDecimal("10.00"), 100);
        sector.setId(1L);
    }
    
    @Test
    void testCalculateRevenueSuccess() {
        when(sectorRepository.findBysectorCode(anyString()))
            .thenReturn(Optional.of(sector));
        
        when(sessionRepository.sumRevenueByDateAndSector(any(LocalDate.class), any(Sector.class)))
            .thenReturn(new BigDecimal("250.00"));
        
        BigDecimal revenue = revenueService.calculateRevenue(LocalDate.now(), "A");
        
        assertNotNull(revenue);
        assertEquals(new BigDecimal("250.00"), revenue);
    }
    
    @Test
    void testCalculateRevenueZero() {
        when(sectorRepository.findBysectorCode(anyString()))
            .thenReturn(Optional.of(sector));
        
        when(sessionRepository.sumRevenueByDateAndSector(any(LocalDate.class), any(Sector.class)))
            .thenReturn(null);
        
        BigDecimal revenue = revenueService.calculateRevenue(LocalDate.now(), "A");
        
        assertNotNull(revenue);
        assertEquals(BigDecimal.ZERO, revenue);
    }
    
    @Test
    void testCalculateRevenueSectorNotFound() {
        when(sectorRepository.findBysectorCode(anyString()))
            .thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, 
            () -> revenueService.calculateRevenue(LocalDate.now(), "Z"));
    }
}
