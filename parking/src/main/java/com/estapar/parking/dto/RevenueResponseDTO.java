package com.estapar.parking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RevenueResponseDTO {
    
    private BigDecimal amount;
    private String currency;
    private LocalDateTime timestamp;
    
    // Constructors
    public RevenueResponseDTO() {
        this.currency = "BRL";
        this.timestamp = LocalDateTime.now();
    }
    
    public RevenueResponseDTO(BigDecimal amount) {
        this.amount = amount;
        this.currency = "BRL";
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters and Setters
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
