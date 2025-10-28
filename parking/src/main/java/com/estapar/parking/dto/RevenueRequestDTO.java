package com.estapar.parking.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RevenueRequestDTO {
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    private String sector;
    
    public RevenueRequestDTO() {}
    
    public RevenueRequestDTO(LocalDate date, String sector) {
        this.date = date;
        this.sector = sector;
    }
    
    public LocalDate getDate() {
        return date;
    }
    
    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    public String getSector() {
        return sector;
    }
    
    public void setSector(String sector) {
        this.sector = sector;
    }
}
