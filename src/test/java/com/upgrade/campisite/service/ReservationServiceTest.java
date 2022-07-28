package com.upgrade.campisite.service;

import com.upgrade.campisite.model.Reservation;
import com.upgrade.campisite.repository.ReservationRepository;
import com.upgrade.campisite.service.validation.ReservationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ReservationServiceTest {

    private static final long RESERVATION_ID = 1;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ReservationValidator reservationValidator;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    public void init() {
        when(reservationRepository.save(any(Reservation.class))).then(param -> {
            Reservation reservation = param.getArgument(0);
            reservation.setId(RESERVATION_ID);
            return reservation;
        });
    }

    @Test
    public void shouldReturnCreatedReservationId() {
        Reservation reservation = getReservation();

        long reservationId = reservationService.book(reservation);

        assertEquals(RESERVATION_ID, reservationId);
    }

    @Test
    public void shouldUpdateReservationById() {
        Reservation reservationDB = getReservation();
        doReturn(reservationDB).when(reservationRepository).getReferenceById(RESERVATION_ID);

        Reservation reservation = getReservation();
        reservation.setName(reservation.getName() + " updated");
        reservation.setEmail("updated-" + reservation.getEmail());
        reservation.setArrivalDate(reservation.getArrivalDate().plusDays(1));
        reservation.setDepartureDate(reservation.getDepartureDate().plusDays(1));

        reservationService.modify(RESERVATION_ID, reservation);

        verify(reservationRepository, times(1)).save(reservationDB);
    }

    @Test
    public void shouldThrowNoSuchElementExceptionWhenReservationIdNotFoundForModify() {
        Reservation reservation = getReservation();

        assertThrows(NoSuchElementException.class, () ->
                reservationService.modify(RESERVATION_ID, reservation)
        );
    }

    @Test
    public void shouldThrowUnsupportedOperationExceptionWhenReservationIsCanceled() {
        Reservation reservationDB = getReservation();
        reservationDB.setCancellationDate(LocalDateTime.now());
        doReturn(reservationDB).when(reservationRepository).getReferenceById(RESERVATION_ID);

        Reservation reservation = getReservation();

        assertThrows(UnsupportedOperationException.class, () ->
                reservationService.modify(RESERVATION_ID, reservation)
        );
    }

    @Test
    public void shouldCancelReservationById() {
        Reservation reservationDB = getReservation();
        doReturn(reservationDB).when(reservationRepository).getReferenceById(RESERVATION_ID);

        reservationService.cancel(RESERVATION_ID);

        verify(reservationRepository, times(1)).save(reservationDB);
    }

    @Test
    public void shouldThrowNoSuchElementExceptionWhenReservationIdNotFoundForCancel() {
        assertThrows(NoSuchElementException.class, () ->
                reservationService.cancel(RESERVATION_ID)
        );
    }

    @Test
    public void shouldReturnReservationWhenValidId() {
        Reservation reservationDB = getReservation();
        doReturn(reservationDB).when(reservationRepository).getReferenceById(RESERVATION_ID);

        Reservation reservation = reservationService.getReservationById(RESERVATION_ID);

        assertEquals(reservationDB, reservation);
    }

    @Test
    public void shouldThrowNoSuchElementExceptionWhenReservationIdNotFoundForGetById() {
        assertThrows(NoSuchElementException.class, () ->
                reservationService.getReservationById(RESERVATION_ID)
        );
    }

    public static Reservation getReservation() {
        Reservation reservation = new Reservation();
        reservation.setName("Rafael Andrade");
        reservation.setEmail("rafadev88@gmail.com");
        reservation.setArrivalDate(LocalDate.of(2022, 8, 1));
        reservation.setDepartureDate(LocalDate.of(2022, 8, 3));
        return reservation;
    }
}