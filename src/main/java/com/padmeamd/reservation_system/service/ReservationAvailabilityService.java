package com.padmeamd.reservation_system.service;

import com.padmeamd.reservation_system.ReservationStatus;
import com.padmeamd.reservation_system.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationAvailabilityService {
private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityService.class);

    public ReservationAvailabilityService(ReservationRepository repository) {
        this.repository = repository;
    }

    private final ReservationRepository repository;

    public boolean isReservationAvailable(Long roomId, LocalDate startDate, LocalDate endDate) {

        List<Long> conflictedIds = repository.findConflictedReservationIds(roomId, startDate, endDate, ReservationStatus.APPROVED);
        if (!endDate.isAfter(startDate)){
            throw new IllegalArgumentException("Start date should be at least 1 day earlier than end date!");
        }
        if (conflictedIds.isEmpty()) {
            return true;
        }
        log.info("Conflicted bookings with ids={}", conflictedIds);
        return false;
    }
}
