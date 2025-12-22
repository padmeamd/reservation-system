package com.padmeamd.reservation_system.controller;

import com.padmeamd.reservation_system.service.Reservation;
import com.padmeamd.reservation_system.service.ReservationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }
    @GetMapping("/{id}")
    public Reservation getReservationById(@PathVariable Long id){
    return reservationService.getReservationById(id);
    }

    @GetMapping()
    public List<Reservation> getAllReservations(){
        return reservationService.findAllReservations();
    }
}
