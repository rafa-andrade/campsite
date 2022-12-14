package com.upgrade.campisite.service;

import com.upgrade.campisite.model.Reservation;
import com.upgrade.campisite.model.ReservationDay;
import com.upgrade.campisite.repository.ReservationRepository;
import com.upgrade.campisite.service.validation.ReservationValidator;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;

@Service
public class ReservationService {

    private ReservationRepository reservationRepository;
    private ReservationValidator reservationValidator;

    public ReservationService(ReservationRepository reservationRepository, ReservationValidator reservationValidator) {
        this.reservationRepository = reservationRepository;
        this.reservationValidator = reservationValidator;
    }

    @Transactional
    @CacheEvict(cacheNames=ReservedDateService.RESERVATION_CACHE_NAME, allEntries=true)
    public long book(Reservation reservation) {
        reservationValidator.validate(reservation);
        reservation.setBookingDate(now());
        reservationRepository.save(reservation);
        return reservation.getId();
    }

    @Transactional
    @CacheEvict(cacheNames=ReservedDateService.RESERVATION_CACHE_NAME, allEntries=true)
    public void modify(long reservationId, Reservation reservationUpdate) {
        Reservation reservation = ofNullable(reservationRepository.getReferenceById(reservationId))
                .orElseThrow(() -> new NoSuchElementException("No reservation found"));
        reservationUpdate.setId(reservation.getId());

        reservationValidator.validate(reservationUpdate);

        if (nonNull(reservation.getCancellationDate())) {
            throw new UnsupportedOperationException("Not able to modify canceled reservation");
        }

        reservation.getDays().removeAll(reservation.getDays().stream().filter(day -> !reservationUpdate.getDays().contains(day)).collect(Collectors.toList()));
        reservation.getDays().addAll(reservationUpdate.getDays().stream().filter(day -> !reservation.getDays().contains(day)).collect(Collectors.toList()));
        reservationUpdate.getDays().forEach(day -> day.setReservation(reservation));

        reservation.setModificationDate(now());
        reservation.setArrivalDate(reservationUpdate.getArrivalDate());
        reservation.setDepartureDate(reservationUpdate.getDepartureDate());
        reservation.setEmail(reservationUpdate.getEmail());
        reservation.setName(reservationUpdate.getName());

        reservationRepository.save(reservation);
    }

    @Transactional
    @CacheEvict(cacheNames=ReservedDateService.RESERVATION_CACHE_NAME, allEntries=true)
    public void cancel(long reservationId) {
        Reservation reservation = ofNullable(reservationRepository.getReferenceById(reservationId))
                .orElseThrow(() -> new NoSuchElementException("No reservation found"));
        reservation.setCancellationDate(now());
        reservationRepository.save(reservation);
    }

    public Reservation getReservationById(long reservationId) {
        return ofNullable(reservationRepository.getReferenceById(reservationId))
                .orElseThrow(() -> new NoSuchElementException("No reservation found"));
    }
}