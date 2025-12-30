package com.padmeamd.reservation_system.service;


import com.padmeamd.reservation_system.ReservationStatus;
import com.padmeamd.reservation_system.entity.Reservation;
import com.padmeamd.reservation_system.entity.ReservationEntity;
import org.springframework.stereotype.Service;
import com.padmeamd.reservation_system.repository.ReservationRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ReservationService {

    private final Map<Long, Reservation> reservationMap;

    private final AtomicLong idCounter;

    private final ReservationRepository repository;

    public ReservationService(ReservationRepository repository) {
        this.repository = repository;
        reservationMap = new HashMap<>();
        idCounter = new AtomicLong();
    }


    public Reservation getReservationById(Long id) {

        if(!reservationMap.containsKey(id)){
            throw new NoSuchElementException("Sorry! Reservation with id "+ id + " does not exist.");
        }
        return reservationMap.get(id);

    }


    public List<Reservation> findAllReservations() {
        List<ReservationEntity> allEntities = repository.findAll();
        List<Reservation> reservationList = allEntities.stream()
                .map(it ->
                    new Reservation(
                            it.getId(),
                            it.getUserId(),
                            it.getRoomId(),
                            it.getStartDate(),
                            it.getEndDate(),
                            it.getStatus()
                    )
                ).toList();
        return reservationList;
    }

    public Reservation createReservation(Reservation reservationToCreate) {
        if(reservationToCreate.id() != null){
            throw new IllegalArgumentException("Id should be empty!");
        }
        if(reservationToCreate.status() != null){
            throw new IllegalArgumentException("Status should be empty!");
        }
        var newReservation = new Reservation(
                idCounter.incrementAndGet(),
                reservationToCreate.userId(),
                reservationToCreate.roomId(),
                reservationToCreate.startDate(),
                reservationToCreate.endDate(),
                ReservationStatus.PENDING
        );
        reservationMap.put(newReservation.id(),newReservation);
        return newReservation;
    }

    public Reservation updateReservation(Long id,Reservation reservationToUpdate) {
        if (!reservationMap.containsKey(id)) {
            throw new NoSuchElementException("Reservation does not exist!");
        }
        var reservation = reservationMap.get(id);
        if (reservation.status() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Cannot modify reservation: status - " + reservation.status());
        }
        var updatedReservation = new Reservation(
                reservation.id(),
                reservationToUpdate.userId(),
                reservationToUpdate.roomId(),
                reservationToUpdate.startDate(),
                reservationToUpdate.endDate(),
                ReservationStatus.PENDING
        );
        reservationMap.put(reservation.id(), updatedReservation);
        return updatedReservation;
    }

    public void deleteReservation(Long id) {
        if(!reservationMap.containsKey(id)){
            throw new NoSuchElementException("Sorry! Reservation with id "+ id + " does not exist.");
        }
        reservationMap.remove(id);
    }

    public Reservation approveReservation(Long id) {
        if(!reservationMap.containsKey(id)){
            throw new NoSuchElementException("Sorry! Reservation with id "+ id + " does not exist.");
        }
        var reservation = reservationMap.get(id);
        if(reservation.status() != ReservationStatus.PENDING){
            throw new IllegalStateException("Cannot approve reservation: status - " + reservation.status());
        }
        var isConflict = isReservationConflicted(reservation);
        if (isConflict){
            throw new IllegalStateException("Cannot approve. There is a conflict");
        }
        var approvedReservation = new Reservation(
                reservation.id(),
                reservation.userId(),
                reservation.roomId(),
                reservation.startDate(),
                reservation.endDate(),
                ReservationStatus.APPROVED
        );
        reservationMap.put(reservation.id(), approvedReservation);
        return  approvedReservation;
    }

    private boolean isReservationConflicted(Reservation reservation){
        for(Reservation existingReservation : reservationMap.values()){
            if(reservation.id().equals(existingReservation.id())){
                continue;
            }
            if(reservation.roomId().equals(existingReservation.roomId())){
                continue;
            }
            if(!existingReservation.status().equals(ReservationStatus.APPROVED)){
                continue;
            }
            if(reservation.startDate().isBefore(existingReservation.endDate()) && existingReservation.startDate().isBefore(reservation.endDate())){
            return true;
            }
        }
        return false;
    }
}
