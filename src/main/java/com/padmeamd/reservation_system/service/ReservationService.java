package com.padmeamd.reservation_system.service;


import com.padmeamd.reservation_system.ReservationStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class ReservationService {

    private final Map<Long, Reservation> reservationMap = Map.of(
            1L, new
                    Reservation(1L,
                    1L,
                    12L, LocalDate.now(),
                    LocalDate.now().plusDays(5),
                    ReservationStatus.APPROVED),
            2L, new
                    Reservation(2L,
                    9L,
                    30L, LocalDate.now(),
                    LocalDate.now().plusDays(5),
                    ReservationStatus.APPROVED),
            3L, new
                    Reservation(3L,
                    8L,
                    129L, LocalDate.now(),
                    LocalDate.now().plusDays(5),
                    ReservationStatus.APPROVED)

    );

    public Reservation getReservationById(Long id) {

        if(!reservationMap.containsKey(id)){
            throw new NoSuchElementException("Sorry! Reservation with id "+ id + " does not exist.");
        }
        return reservationMap.get(id);

    }


    public List<Reservation> findAllReservations() {
        return reservationMap.values().stream().toList();
    }
}
