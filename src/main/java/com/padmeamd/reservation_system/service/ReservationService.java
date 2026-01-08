package com.padmeamd.reservation_system.service;


import com.padmeamd.reservation_system.ReservationStatus;
import com.padmeamd.reservation_system.entity.Reservation;
import com.padmeamd.reservation_system.entity.ReservationEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.padmeamd.reservation_system.repository.ReservationRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository repository;

    public Reservation getReservationById(Long id) {
        ReservationEntity reservationEntity = repository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("Sorry! Reservation with id " + id + " does not exist."));
        return toDomainReservation(reservationEntity);
    }

    public List<Reservation> findAllReservations() {
        List<ReservationEntity> allEntities = repository.findAll();
        List<Reservation> reservationList = allEntities.stream()
                .map(this::toDomainReservation).toList();
        return reservationList;
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if (reservationToCreate.status() != null) {
            throw new IllegalArgumentException("Status should be empty!");
        }
        if(!reservationToCreate.endDate().isAfter(reservationToCreate.startDate())) {
            throw new IllegalArgumentException("Start date should be at least 1 day earlier than end date!");
        }
        var entityToSave = new ReservationEntity(
                null,
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );
        var savedEntity = repository.save(entityToSave);
        return toDomainReservation(savedEntity);
    }

    public Reservation updateReservation(Long id, Reservation reservationToUpdate) {
        var reservationEntity = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Sorry! Reservation with id " + id + " does not exist."));

        if (reservationEntity.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation: status - " + reservationEntity.getStatus());
        }
        if(!reservationToUpdate.endDate().isAfter(reservationToUpdate.startDate())) {
            throw new IllegalArgumentException("Start date should be at least 1 day earlier than end date!");
        }
        var reservationToSave = new ReservationEntity(
                reservationEntity.getId(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );
        var updatedReservation = repository.save(reservationToSave);
        return toDomainReservation(updatedReservation);
    }

 @Transactional
    public void cancelReservation(Long id) {
        var reservation = repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Sorry! Reservation with id " + id + " does not exist."));
     if(reservation.getStatus().equals(ReservationStatus.APPROVED)) {
         throw new IllegalStateException("You can't cancel approved reservation. Please, contact the manager.");
     }
     if(reservation.getStatus().equals(ReservationStatus.CANCELLED)) {
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
        var isConflict = isReservationConflicted(reservationEntity);
        if (isConflict) {
            throw new IllegalStateException("Cannot approve. There is a conflict");
        }
        reservationEntity.setStatus(ReservationStatus.APPROVED);
        repository.save(reservationEntity);

        return toDomainReservation(reservationEntity);
    }

    private boolean isReservationConflicted(ReservationEntity reservation) {
        var allReservations = repository.findAll();
        for (ReservationEntity existingReservation : allReservations) {
            if (reservation.getId().equals(existingReservation.getId())) {
                continue;
            }
            if (!reservation.getRoomId().equals(existingReservation.getRoomId())) {
                continue;
            }
            if (!reservation.getStatus().equals(ReservationStatus.APPROVED)) {
                continue;
            }
            if (reservation.getStartDate().isBefore(existingReservation.getEndDate()) && existingReservation.getStartDate().isBefore(reservation.getEndDate())) {
                return true;
            }
        }

        return false;
    }

    private Reservation toDomainReservation(ReservationEntity reservation) {
        return new Reservation(
                reservation.getId(),
                reservation.getUserId(),
                reservation.getRoomId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getStatus()
        );
    }
}
