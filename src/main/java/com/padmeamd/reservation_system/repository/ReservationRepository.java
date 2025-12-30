package com.padmeamd.reservation_system.repository;

import com.padmeamd.reservation_system.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

}
