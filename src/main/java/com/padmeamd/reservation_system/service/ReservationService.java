package com.padmeamd.reservation_system.service;


import com.padmeamd.reservation_system.ReservationStatus;
import com.padmeamd.reservation_system.entity.Reservation;
import com.padmeamd.reservation_system.entity.ReservationEntity;
import com.padmeamd.reservation_system.entity.ReservationSearchFilter;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import com.padmeamd.reservation_system.mapper.ReservationMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.padmeamd.reservation_system.repository.ReservationRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
public class ReservationService {

    private final ReservationRepository repository;
    private final ReservationAvailabilityService reservationAvailabilityService;

    public ReservationService(ReservationRepository repository, ReservationAvailabilityService reservationAvailabilityService, ReservationMapper mapper) {
        this.repository = repository;
        this.reservationAvailabilityService = reservationAvailabilityService;
        this.mapper = mapper;
    }

    private final ReservationMapper mapper;


    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = repository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sorry! Reservation with id " + id + " does not exist."));
        return mapper.toDomain(reservationEntity);
    }

    public List<Reservation> findAllByFilter( ReservationSearchFilter filter) {
        int pageSize = filter.pageSize() != null ? filter.pageSize() : 10;
        int pageNumber = filter.pageNumber() != null ? filter.pageNumber() : 0;
        var pageable = Pageable.ofSize(pageSize).withPage(pageNumber);
        List<ReservationEntity> allEntities = repository.findAllByFilter(filter.roomId(), filter.userId(), pageable);
        List<Reservation> reservationList = allEntities.stream()
                .map(mapper::toDomain).toList();
        return reservationList;
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status should be empty!");
        }
        if (!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("Start date should be at least 1 day earlier than end date!");
        }
        var entityToSave = mapper.toEntity(reservationToCreate);
        entityToSave.setStatus(ReservationStatus.PENDING);
        var savedEntity = repository.save(entityToSave);
        return mapper.toDomain(savedEntity);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        var reservationEntity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Sorry! Reservation with id " + id + " does not exist."));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation: status - " + reservationEntity.getStatus());
        }
        if (!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("Start date should be at least 1 day earlier than end date!");
        }
        var reservationToSave = mapper.toEntity(reservationToUpdate);
        reservationToSave.setId(reservationEntity.getId());
        reservationToSave.setStatus(ReservationStatus.PENDING);
        var updatedReservation = repository.save(reservationToSave);
        return mapper.toDomain(updatedReservation);
    }

    @Transactional
    public void cancelReservation(Long id) {
        var reservation = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Sorry! Reservation with id " + id + " does not exist."));
        if (reservation.getStatus().equals(ReservationStatus.APPROVED)) {
            throw new IllegalStateException("You can't cancel approved reservation. Please, contact the manager.");
        }
        if (reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
            throw new IllegalStateException("The reservation has already been cancelled.");

        }
        repository.setStatus(id, ReservationStatus.CANCELLED);
        log.info("SUCCESS: Reservation with id " + id + " has been cancelled.");
    }

    public Reservation approveReservation(Long id) {
        var reservationEntity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Sorry! Reservation with id " + id + " does not exist."));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot approve reservation: status - " + reservationEntity.getStatus());
        }
        var isAvailableToApprove = reservationAvailabilityService.isReservationAvailable(reservationEntity.getRoomId(), reservationEntity.getStartDate(), reservationEntity.getEndDate());
        if (isAvailableToApprove) {
            throw new IllegalStateException("Cannot approve. There is a conflict");
        }
        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);

        return mapper.toDomain(reservationEntity);
    }

}
