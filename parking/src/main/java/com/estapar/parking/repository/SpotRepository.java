package com.estapar.parking.repository;

import com.estapar.parking.model.Spot;
import com.estapar.parking.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpotRepository extends JpaRepository<Spot, Long> {
    List<Spot> findBySectorAndOccupied(Sector sector, Boolean occupied);
    Long countBySectorAndOccupied(Sector sector, Boolean occupied);
}
