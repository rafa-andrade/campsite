package com.upgrade.campisite.service;

import com.upgrade.campisite.model.Reservation;
import com.upgrade.campisite.model.ReservationDay;
import com.upgrade.campisite.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@SpringBootTest
public class ReservedDateServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservedDateService reservedDateService;

    @Test
    public void shouldReturnAllReservedDates() {
        Reservation reservation1 = ReservationServiceTest.getReservation();
        reservation1.setArrivalDate(LocalDate.of(2022, 8, 1));
        reservation1.setDepartureDate(LocalDate.of(2022, 8, 3));
        reservation1.loadDays();

        Reservation reservation2 = ReservationServiceTest.getReservation();
        reservation2.setArrivalDate(LocalDate.of(2022, 8, 5));
        reservation2.setDepartureDate(LocalDate.of(2022, 8, 6));
        reservation2.loadDays();

        Reservation reservation3 = ReservationServiceTest.getReservation();
        reservation3.setArrivalDate(LocalDate.of(2022, 8, 9));
        reservation3.setDepartureDate(LocalDate.of(2022, 8, 9));
        reservation3.loadDays();

        LocalDate from = LocalDate.of(2022, 8, 1);
        LocalDate to = LocalDate.of(2022, 8, 10);

        List<ReservationDay> reservedDays = new ArrayList<>();
        reservedDays.addAll(reservation1.getDays());
        reservedDays.addAll(reservation2.getDays());
        reservedDays.addAll(reservation3.getDays());

        doReturn(reservedDays).when(reservationRepository).getReservationDays(from, to);

        Map<LocalDate, Reservation> reservedDates = reservedDateService.getReservedDates(from, to);

        assertEquals(6, reservedDates.size());

        assertEquals(reservation1, reservedDates.get(LocalDate.of(2022, 8, 1)));
        assertEquals(reservation1, reservedDates.get(LocalDate.of(2022, 8, 2)));
        assertEquals(reservation1, reservedDates.get(LocalDate.of(2022, 8, 3)));

        assertEquals(reservation2, reservedDates.get(LocalDate.of(2022, 8, 5)));
        assertEquals(reservation2, reservedDates.get(LocalDate.of(2022, 8, 6)));

        assertEquals(reservation3, reservedDates.get(LocalDate.of(2022, 8, 9)));
    }
}