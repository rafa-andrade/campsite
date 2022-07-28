package com.upgrade.campisite.service;

import com.upgrade.campisite.model.Reservation;
import com.upgrade.campisite.model.ReservationDay;
import com.upgrade.campisite.repository.ReservationRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Service
public class ReservedDateService {
    public static final String RESERVATION_CACHE_NAME = "ReservedDates";

    private ReservationRepository reservationRepository;

    public ReservedDateService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Cacheable(cacheNames=RESERVATION_CACHE_NAME)
    public Map<LocalDate, Reservation> getReservedDates(LocalDate from, LocalDate to) {
        return reservationRepository.getReservationDays(from, to).stream()
                .collect(toMap(ReservationDay::getReservationDay, ReservationDay::getReservation));
    }
}