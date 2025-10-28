package com.estapar.parking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parking_sessions")
public class ParkingSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "license_plate", nullable = false, length = 20)
    private String licensePlate;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = true)
    private Spot spot;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = true)
    private Sector sector;
    
    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;
    
    @Column(name = "parked_time")
    private LocalDateTime parkedTime;
    
    @Column(name = "exit_time")
    private LocalDateTime exitTime;
    
    @Column(name = "applied_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal appliedPrice;
    
    @Column(name = "final_amount", precision = 10, scale = 2)
    private BigDecimal finalAmount;
    
    public ParkingSession() {}
    
    public ParkingSession(String licensePlate, Sector sector, LocalDateTime entryTime, BigDecimal appliedPrice) {
        this.licensePlate = licensePlate;
        this.sector = sector;
        this.entryTime = entryTime;
        this.appliedPrice = appliedPrice;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getLicensePlate() {
        return licensePlate;
    }
    
    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }
    
    public Spot getSpot() {
        return spot;
    }
    
    public void setSpot(Spot spot) {
        this.spot = spot;
    }
    
    public Sector getSector() {
        return sector;
    }
    
    public void setSector(Sector sector) {
        this.sector = sector;
    }
    
    public LocalDateTime getEntryTime() {
        return entryTime;
    }
    
    public void setEntryTime(LocalDateTime entryTime) {
        this.entryTime = entryTime;
    }
    
    public LocalDateTime getParkedTime() {
        return parkedTime;
    }
    
    public void setParkedTime(LocalDateTime parkedTime) {
        this.parkedTime = parkedTime;
    }
    
    public LocalDateTime getExitTime() {
        return exitTime;
    }
    
    public void setExitTime(LocalDateTime exitTime) {
        this.exitTime = exitTime;
    }
    
    public BigDecimal getAppliedPrice() {
        return appliedPrice;
    }
    
    public void setAppliedPrice(BigDecimal appliedPrice) {
        this.appliedPrice = appliedPrice;
    }
    
    public BigDecimal getFinalAmount() {
        return finalAmount;
    }
    
    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }
}
