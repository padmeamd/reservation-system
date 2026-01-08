package com.padmeamd.reservation_system.entity;

public record ReservationSearchFilter( Long roomId,
                                       Long userId,
                                       Integer pageSize,
                                       Integer pageNumber) {
}
