package com.estapar.parking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class SectorConfigDTO {
    
    @JsonProperty("sector")
    private String sector;
    
    @JsonProperty("basePrice")
    private BigDecimal basePrice;
    
    @JsonProperty("max_capacity")
    private Integer maxCapacity;
    
    // Constructors
    public SectorConfigDTO() {}
    
    // Getters and Setters
    public String getSector() {
        return sector;
    }
    
    public void setSector(String sector) {
        this.sector = sector;
    }
    
    public BigDecimal getBasePrice() {
        return basePrice;
    }
    
    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }
    
    public Integer getMaxCapacity() {
        return maxCapacity;
    }
    
    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
}
