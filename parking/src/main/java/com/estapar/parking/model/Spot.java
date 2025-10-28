package com.estapar.parking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "spots")
public class Spot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @Column(name = "spot_code", nullable = false, unique = true, length = 10)
    private String spotCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sector_id", nullable = false)
    private Sector sector;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "occupied", nullable = false)
    private Boolean occupied = false;

    @Column(name = "occupied_by", length = 20)
    private String occupiedBy;

    @Column(name = "occupied_at")
    private LocalDateTime occupiedAt;

    public Spot() {}

    public Spot(String spotCode, Double longitude, Double latitude, Sector sector) {
        this.spotCode = spotCode;
        this.sector = sector;
        this.latitude = latitude;
        this.longitude = longitude;
        this.occupied = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSpotCode() {
        return spotCode;
    }

    public void setSpotCode(String spotCode) {
        this.spotCode = spotCode;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Boolean getOccupied() {
        return occupied;
    }

    public void setOccupied(Boolean occupied) {
        this.occupied = occupied;
    }

    public String getOccupiedBy() {
        return occupiedBy;
    }

    public void setOccupiedBy(String occupiedBy) {
        this.occupiedBy = occupiedBy;
    }

    public LocalDateTime getOccupiedAt() {
        return occupiedAt;
    }

    public void setOccupiedAt(LocalDateTime occupiedAt) {
        this.occupiedAt = occupiedAt;
    }

    @Override
    public String toString() {
        return "Spot{" +
                "id=" + id +
                ", spotCode='" + spotCode + '\'' +
                ", sector=" + (sector != null ? sector.getSectorCode() : null) +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", occupied=" + occupied +
                '}';
    }
}
