package com.upgrade.campisite.service;

import com.upgrade.campisite.dto.AvailableDateDTO;
import com.upgrade.campisite.model.Reservation;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
public class AvailableDateService {

    private ReservedDateService reservedDateService;

    public AvailableDateService(ReservedDateService reservedDateService) {
        this.reservedDateService = reservedDateService;
    }

    public List<AvailableDateDTO> getAvailableDates(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' param must be before 'to'");
        }

        LocalDate firstDayOfMonthFrom = from.withDayOfMonth(1);
        LocalDate lastDayOfMonthTo = to.with(lastDayOfMonth());

        Map<LocalDate, Reservation> reservedDates = Stream.iterate(firstDayOfMonthFrom, date -> date.plusMonths(1))
                .limit(MONTHS.between(firstDayOfMonthFrom, lastDayOfMonthTo) + 1)
                .flatMap(date -> reservedDateService.getReservedDates(date, date.with(lastDayOfMonth())).entrySet().stream())
                .collect(toMap(Entry::getKey, Entry::getValue));

        return Stream.iterate(from, date -> date.plusDays(1))
                .limit(DAYS.between(from, to) + 1)
                .filter(date -> !reservedDates.containsKey(date))
                .map(AvailableDateDTO::new)
                .collect(toList());
    }
}