package com.upgrade.campisite.service.validation;

import com.upgrade.campisite.model.Reservation;
import com.upgrade.campisite.service.ReservedDateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ReservationValidator {

    private int maxDays;
    private int minimumDaysAhead;
    private int upToMonths;
    private ReservedDateService reservedDateService;

    public ReservationValidator(@Value("${reservation.validator.max-days}") int maxDays,
                                @Value("${reservation.validator.minimum-days-ahead-of-arrival}") int minimumDaysAhead,
                                @Value("${reservation.validator.up-to-months-in-advance}") int upToMonths,
                                ReservedDateService reservedDateService) {
        this.maxDays = maxDays;
        this.minimumDaysAhead = minimumDaysAhead;
        this.upToMonths = upToMonths;
        this.reservedDateService = reservedDateService;
    }

    public boolean validate(Reservation reservation) {
        if (reservation.getArrivalDate().isAfter(reservation.getDepartureDate())) {
            throw new IllegalArgumentException("Arrival date must be before departure date");
        }

        LocalDateTime arrivalDateTime = reservation.getArrivalDate().atTime(12, 0, 0, 0); //default check-in time

        if (now().isAfter(arrivalDateTime)) {
            throw new IllegalArgumentException("Arrival date in the past");
        }

        if (ChronoUnit.DAYS.between(now(), arrivalDateTime) < minimumDaysAhead) {
            throw new IllegalArgumentException("Minimum " + minimumDaysAhead + " day ahead of arrival");
        }

        if (arrivalDateTime.isAfter(now().plusMonths(upToMonths))) {
            throw new IllegalArgumentException("Up to " + upToMonths + " month in advance");
        }

        if (ChronoUnit.DAYS.between(reservation.getArrivalDate(), reservation.getDepartureDate()) > maxDays) {
            throw new IllegalArgumentException("Reserved for max " + maxDays + " days");
        }

        LocalDate firstDayOfMonthArrival = reservation.getArrivalDate().withDayOfMonth(1);
        LocalDate lastDayOfMonthDeparture = reservation.getDepartureDate().with(TemporalAdjusters.lastDayOfMonth());

        //find reservedDates by month to use cache
        Map<LocalDate, Reservation> reservedDates = Stream.iterate(firstDayOfMonthArrival, date -> date.plusMonths(1))
                .limit(ChronoUnit.MONTHS.between(firstDayOfMonthArrival, lastDayOfMonthDeparture) + 1)
                .flatMap(date -> reservedDateService.getReservedDates(date, date.with(TemporalAdjusters.lastDayOfMonth())).entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        reservation.loadDays();
        if (reservation.getDays().stream().anyMatch(day -> reservedDates.containsKey(day.getReservationDay()))) {
            throw new IllegalArgumentException("Selected range is already reserved");
        }
        return true;
    }

    protected LocalDateTime now() { //to enable fixed dates in the test
        return LocalDateTime.now();
    }
}
