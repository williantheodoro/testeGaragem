package com.estapar.parking.controller;

import com.estapar.parking.dto.RevenueRequestDTO;
import com.estapar.parking.dto.RevenueResponseDTO;
import com.estapar.parking.service.RevenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/revenue")
public class RevenueController {
    
    @Autowired
    private RevenueService revenueService;
    
    @GetMapping
    public ResponseEntity<RevenueResponseDTO> getRevenue(@RequestBody RevenueRequestDTO request) {
        try {
            BigDecimal amount = revenueService.calculateRevenue(
                request.getDate(),
                request.getSector()
            );
            
            RevenueResponseDTO response = new RevenueResponseDTO(amount);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
