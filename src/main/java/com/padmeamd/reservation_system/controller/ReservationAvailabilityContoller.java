package com.padmeamd.reservation_system.controller;

import com.padmeamd.reservation_system.entity.AvailabilityStatus;
import com.padmeamd.reservation_system.entity.CheckAvailabilityRequest;
import com.padmeamd.reservation_system.entity.CheckAvailabilityResponse;
import com.padmeamd.reservation_system.service.ReservationAvailabilityService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reservation/availability")

public class ReservationAvailabilityContoller {

    private static final Logger log = LoggerFactory.getLogger(ReservationAvailabilityContoller.class);

    private final ReservationAvailabilityService service;

    public ReservationAvailabilityContoller(ReservationAvailabilityService service) {
        this.service = service;
    }


    @PostMapping("/check")
    public ResponseEntity<CheckAvailabilityResponse> checkAvailability(@Valid CheckAvailabilityRequest request){
    log.info("Called method checkAvailability: request={}",request);
    boolean isAvailable = service.isReservationAvailable(request.roomId(), request.startDate(),request.endDate());
    var message = isAvailable?"Reservation successfully available.":"Reservation is not available.";
    var status = isAvailable? AvailabilityStatus.AVAILABLE :AvailabilityStatus.BOOKED;
    return ResponseEntity.ok(new CheckAvailabilityResponse(message,status));
    }
}
