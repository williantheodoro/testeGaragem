package com.estapar.parking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SpotConfigDTO {
    
    @JsonProperty("id")
    private String id;

    @JsonProperty("spotCode")
    private String spotCode; 
    
    @JsonProperty("sector")
    private String sector;
    
    @JsonProperty("lat")
    private Double lat;
    
    @JsonProperty("lng")
    private Double lng;
    
    public SpotConfigDTO() {}
    
    public SpotConfigDTO(String id, String spotCode, String sector, Double lat, Double lng) {
        this.id = id;
        this.spotCode = spotCode;
        this.sector = sector;
        this.lat = lat;
        this.lng = lng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSpotCode() {
        return spotCode;
    }

    public void setSpotCode(String spotCode) {
        this.spotCode = spotCode;
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

    @Override
    public String toString() {
        return "SpotConfigDTO{" +
                "id='" + id + '\'' +
                ", spotCode='" + spotCode + '\'' +
                ", sector='" + sector + '\'' +
                ", lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}
