package com.estapar.parking.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.estapar.parking.model.ParkingSession;
import com.estapar.parking.model.Sector;

@Repository
public interface ParkingSessionRepository extends JpaRepository<ParkingSession, Long> {
    
    Optional<ParkingSession> findByLicensePlateAndExitTimeIsNull(String licensePlate);
    
    @Query("SELECT COALESCE(SUM(ps.finalAmount), 0) FROM ParkingSession ps " +
           "WHERE ps.sector = :sector " +
           "AND CAST(ps.exitTime AS date) = :date " +
           "AND ps.finalAmount IS NOT NULL")
    BigDecimal sumRevenueByDateAndSector(@Param("date") LocalDate date, @Param("sector") Sector sector);
}
