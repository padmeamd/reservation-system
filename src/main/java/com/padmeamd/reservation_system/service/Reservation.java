package com.padmeamd.reservation_system.service;

import com.padmeamd.reservation_system.ReservationStatus;

import java.time.LocalDate;

public record Reservation(
        Long id,
        Long userId,
        Long Room,
        LocalDate startDate,
        LocalDate endDate,
        ReservationStatus status
){ }
