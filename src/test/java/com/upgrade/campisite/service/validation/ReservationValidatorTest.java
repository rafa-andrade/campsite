package com.upgrade.campisite.service.validation;

import com.upgrade.campisite.model.Reservation;
import com.upgrade.campisite.service.ReservationServiceTest;
import com.upgrade.campisite.service.ReservedDateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;

import static java.time.LocalDate.of;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;


@SpringBootTest
public class ReservationValidatorTest {

    private static final LocalDate REFERENCE_DATE = LocalDate.of(2022, 8, 20);

    @Mock
    private ReservedDateService reservedDateService;

    private ReservationValidator reservationValidator;

    @BeforeEach
    public void init() {
        reservationValidator = new ReservationValidator(3, 1, 1, reservedDateService) {
            @Override
            public LocalDateTime now() {
                return REFERENCE_DATE.atTime(12, 0, 0);
            }
        };
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenDepartureIsAfterArrivalDate() {
        Reservation reservation = ReservationServiceTest.getReservation();
        reservation.setArrivalDate(LocalDate.of(2022, 8, 3));
        reservation.setDepartureDate(LocalDate.of(2022, 8, 1));

        assertThrows(IllegalArgumentException.class, () ->
                reservationValidator.validate(reservation)
        );
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenMinimumDaysAheadIsNotRespected() {
        Reservation reservation = ReservationServiceTest.getReservation();

        reservation.setArrivalDate(REFERENCE_DATE);
        reservation.setDepartureDate(REFERENCE_DATE.plusDays(2));

        assertThrows(IllegalArgumentException.class, () ->
                reservationValidator.validate(reservation)
        );
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenArrivalDateInThePast() {
        Reservation reservation = ReservationServiceTest.getReservation();

        reservation.setArrivalDate(REFERENCE_DATE);
        reservation.setDepartureDate(REFERENCE_DATE.plusDays(2));

        assertThrows(IllegalArgumentException.class, () ->
                reservationValidator.validate(reservation)
        );
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenUpToMonthsIsNotRespected() {
        Reservation reservation = ReservationServiceTest.getReservation();

        reservation.setArrivalDate(REFERENCE_DATE.plusMonths(2));
        reservation.setDepartureDate(REFERENCE_DATE.plusMonths(2).plusDays(2));

        assertThrows(IllegalArgumentException.class, () ->
                reservationValidator.validate(reservation)
        );
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenMaxReservationDaysIsNotRespected() {
        Reservation reservation = ReservationServiceTest.getReservation();

        reservation.setArrivalDate(REFERENCE_DATE.plusDays(2));
        reservation.setDepartureDate(REFERENCE_DATE.plusDays(10));

        assertThrows(IllegalArgumentException.class, () ->
                reservationValidator.validate(reservation)
        );
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenPeriodAlreadyReserved() {
        Reservation reservation = ReservationServiceTest.getReservation();
        reservation.setArrivalDate(LocalDate.of(2022, 8, 30));
        reservation.setDepartureDate(LocalDate.of(2022, 9, 1));

        Reservation reservedDate = ReservationServiceTest.getReservation();
        reservedDate.setArrivalDate(of(2022, 9, 1));
        reservedDate.setDepartureDate(of(2022, 9, 1));

        doReturn(new HashMap<>()).when(reservedDateService).getReservedDates(of(2022, 8, 1), of(2022, 8, 31));
        doReturn(Collections.singletonMap(reservedDate.getArrivalDate(), reservedDate)).when(reservedDateService).getReservedDates(of(2022, 9, 1), of(2022, 9, 30));

        assertThrows(IllegalArgumentException.class, () ->
                reservationValidator.validate(reservation)
        );
        verify(reservedDateService, times(1)).getReservedDates(of(2022, 8, 1), of(2022, 8, 31));
        verify(reservedDateService, times(1)).getReservedDates(of(2022, 9, 1), of(2022, 9, 30));
    }

    @Test
    public void shouldReturnTrueWhenPeriodNotReserved() {
        Reservation reservation = ReservationServiceTest.getReservation();
        reservation.setArrivalDate(LocalDate.of(2022, 8, 30));
        reservation.setDepartureDate(LocalDate.of(2022, 9, 1));

        doReturn(new HashMap<>()).when(reservedDateService).getReservedDates(of(2022, 8, 1), of(2022, 8, 31));
        doReturn(new HashMap<>()).when(reservedDateService).getReservedDates(of(2022, 9, 1), of(2022, 9, 30));

        boolean check = reservationValidator.validate(reservation);
        assertTrue(check);
    }
}
