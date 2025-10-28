package com.estapar.parking.dto;

import java.math.BigDecimal;
import java.util.List;

public class GarageConfigDTO {
    private List<SectorConfigDTO> garage;
    private List<SpotConfigDTO> spots;
    
    public GarageConfigDTO() {
    }
    
    public GarageConfigDTO(List<SectorConfigDTO> garage, List<SpotConfigDTO> spots) {
        this.garage = garage;
        this.spots = spots;
    }
    
    public List<SectorConfigDTO> getGarage() {
        return garage;
    }
    
    public void setGarage(List<SectorConfigDTO> garage) {
        this.garage = garage;
    }
    
    public List<SpotConfigDTO> getSpots() {
        return spots;
    }
    
    public void setSpots(List<SpotConfigDTO> spots) {
        this.spots = spots;
    }
    
    // Inner class para Sector
    public static class SectorConfigDTO {
        private String sector;
        private BigDecimal basePrice;
        private Integer max_capacity;
        
        public SectorConfigDTO() {
        }
        
        public SectorConfigDTO(String sector, BigDecimal basePrice, Integer max_capacity) {
            this.sector = sector;
            this.basePrice = basePrice;
            this.max_capacity = max_capacity;
        }
        
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
        
        public Integer getMax_capacity() {
            return max_capacity;
        }
        
        public void setMax_capacity(Integer max_capacity) {
            this.max_capacity = max_capacity;
        }
    }
    
    // Inner class para Spot
    public static class SpotConfigDTO {
        private Long id;
        private String sector;
        private Double lat;
        private Double lng;
        
        public SpotConfigDTO() {
        }
        
        public SpotConfigDTO(Long id, String sector, Double lat, Double lng) {
            this.id = id;
            this.sector = sector;
            this.lat = lat;
            this.lng = lng;
        }
        
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public String getSector() {
            return sector;
        }
        
        public void setSector(String sector) {
            this.sector = sector;
        }
        
        public Double getLat() {
            return lat;
        }
        
        public void setLat(Double lat) {
            this.lat = lat;
        }
        
        public Double getLng() {
            return lng;
        }
        
        public void setLng(Double lng) {
            this.lng = lng;
        }
    }
}
