package com.hackathon.inditex.Repositories;

import com.hackathon.inditex.Entities.Center;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CenterRepository extends JpaRepository<Center, Long> {
    Optional<Center> findByCoordinatesLatitudeAndCoordinatesLongitude(Double latitude, Double longitude);
}