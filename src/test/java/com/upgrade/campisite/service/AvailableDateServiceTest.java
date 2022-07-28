package com.upgrade.campisite.service;

import com.upgrade.campisite.dto.AvailableDateDTO;
import com.upgrade.campisite.model.Reservation;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.LocalDate.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
public class AvailableDateServiceTest {

    @Mock
    private ReservedDateService reservedDateService;

    @InjectMocks
    private AvailableDateService availableDateService;

    @Test
    public void shouldReturnAvailableDates() {
        Reservation reservation1 = ReservationServiceTest.getReservation();
        reservation1.setArrivalDate(of(2022, 8, 15));
        reservation1.setDepartureDate(of(2022, 8, 17));

        Reservation reservation2 = ReservationServiceTest.getReservation();
        reservation2.setArrivalDate(of(2022, 8, 25));
        reservation2.setDepartureDate(of(2022, 8, 26));

        Reservation reservation3 = ReservationServiceTest.getReservation();
        reservation3.setArrivalDate(of(2022, 9, 5));

        Map<LocalDate, Reservation> reservedDates_2022_08 = new HashMap<>();
        reservedDates_2022_08.put(of(2022, 8, 15), reservation1);
        reservedDates_2022_08.put(of(2022, 8, 16), reservation1);
        reservedDates_2022_08.put(of(2022, 8, 17), reservation1);
        reservedDates_2022_08.put(of(2022, 8, 25), reservation2);
        reservedDates_2022_08.put(of(2022, 8, 26), reservation2);

        doReturn(reservedDates_2022_08).when(reservedDateService).getReservedDates(of(2022, 8, 1), of(2022, 8, 31));

        Map<LocalDate, Reservation> reservedDates_2022_09 = new HashMap<>();
        reservedDates_2022_09.put(of(2022, 9, 5), reservation3);

        doReturn(reservedDates_2022_09).when(reservedDateService).getReservedDates(of(2022, 9, 1), of(2022, 9, 30));

        LocalDate from = of(2022, 8, 10);
        LocalDate to = of(2022, 9, 10);

        List<LocalDate> availableDates = availableDateService.getAvailableDates(from, to)
                .stream()
                .map(AvailableDateDTO::getAvailableDate)
                .sorted()
                .collect(Collectors.toList());

        assertEquals(26, availableDates.size());
        assertEquals(of(2022, 8, 10), availableDates.get(0));
        assertEquals(of(2022, 9, 10), availableDates.get(availableDates.size() - 1));

        Stream.of(reservedDates_2022_08.entrySet(), reservedDates_2022_09.entrySet()).flatMap(Set::stream).forEach(reservedDay ->
                assertFalse(availableDates.contains(reservedDay))
        );
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenFromIsAfterTo() {
        LocalDate from = of(2022, 8, 10);
        LocalDate to = of(2022, 9, 10);

        assertThrows(IllegalArgumentException.class, () ->
                availableDateService.getAvailableDates(to, from)
        );
    }
}