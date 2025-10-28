package com.estapar.parking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebhookEventDTO {
    
    @JsonProperty("license_plate")
    private String licensePlate;
    
    @JsonProperty("entry_time")
    private LocalDateTime entryTime;
    
    @JsonProperty("exit_time")
    private LocalDateTime exitTime;
    
    @JsonProperty("event_type")
    private String eventType;
    
    @JsonProperty("lat")
    private BigDecimal lat;
    
    @JsonProperty("lng")
    private BigDecimal lng;
    
    // Constructors
    public WebhookEventDTO() {}
    
    // Getters and Setters
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
    
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
    
    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }
    
    public LocalDateTime getExitTime() {
        return exitTime;
    }
    
    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public BigDecimal getLat() {
        return lat;
    }
    
    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }
    
    public BigDecimal getLng() {
        return lng;
    }
    
    public void setLng(BigDecimal lng) {
        this.lng = lng;
    }
}
