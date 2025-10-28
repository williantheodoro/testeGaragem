package com.estapar.parking.controller;

import com.estapar.parking.dto.WebhookEventDTO;
import com.estapar.parking.exception.ParkingFullException;
import com.estapar.parking.exception.VehicleAlreadyParkedException;
import com.estapar.parking.exception.VehicleNotFoundException;
import com.estapar.parking.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
public class WebhookController {
    
    @Autowired
    private ParkingService parkingService;
    
    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody WebhookEventDTO event) {
        try {
            String eventType = event.getEventType();
            
            if ("ENTRY".equals(eventType)) {
                parkingService.handleEntry(event);
            } else if ("PARKED".equals(eventType)) {
                parkingService.handleParked(event);
            } else if ("EXIT".equals(eventType)) {
                parkingService.handleExit(event);
            } else {
                return ResponseEntity.badRequest().build();
            }
            
            return ResponseEntity.ok().build();
            
        } catch (ParkingFullException | VehicleAlreadyParkedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (VehicleNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
