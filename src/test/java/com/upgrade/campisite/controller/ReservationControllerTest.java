package com.upgrade.campisite.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upgrade.campisite.model.Reservation;
import com.upgrade.campisite.model.ReservationDay;
import com.upgrade.campisite.service.ReservationService;
import com.upgrade.campisite.service.ReservationServiceTest;
import com.upgrade.campisite.service.ReservedDateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static java.time.LocalDate.of;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ReservationControllerTest {

    private static final Long RESERVATION_ID = 1L;

    @MockBean
    private ReservationService reservationService;

    @MockBean
    private ReservedDateService reservedDateService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    @Test
    public void shouldReturnCreatedIdWhenPostNewReservation() throws Exception {
        doReturn(RESERVATION_ID).when(reservationService).book(any(Reservation.class));

        Reservation reservation = ReservationServiceTest.getReservation();

        mockMvc.perform(post("/reservations")
                        .content(objectMapper.writeValueAsString(reservation))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$", is(RESERVATION_ID.intValue())));
    }

    @Test
    public void shouldReturn200WhenModifyReservation() throws Exception {
        doNothing().when(reservationService).modify(anyLong(), any(Reservation.class));

        Reservation reservation = ReservationServiceTest.getReservation();

        mockMvc.perform(put("/reservations/" + RESERVATION_ID)
                        .content(objectMapper.writeValueAsString(reservation))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldReturn400WhenInvalidModifyReservation() throws Exception {
        doThrow(new IllegalArgumentException()).when(reservationService).modify(anyLong(), any(Reservation.class));

        Reservation reservation = ReservationServiceTest.getReservation();

        mockMvc.perform(put("/reservations/" + RESERVATION_ID)
                        .content(objectMapper.writeValueAsString(reservation))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
    }

    @Test
    public void shouldReturn404WhenModifyInvalidReservationId() throws Exception {
        doThrow(new NoSuchElementException()).when(reservationService).modify(anyLong(), any(Reservation.class));

        Reservation reservation = ReservationServiceTest.getReservation();

        mockMvc.perform(put("/reservations/99")
                        .content(objectMapper.writeValueAsString(reservation))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void shouldReturn405WhenModifyCanceledReservation() throws Exception {
        doThrow(new UnsupportedOperationException()).when(reservationService).modify(anyLong(), any(Reservation.class));

        Reservation reservation = ReservationServiceTest.getReservation();

        mockMvc.perform(put("/reservations/" + RESERVATION_ID)
                        .content(objectMapper.writeValueAsString(reservation))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().is(HttpStatus.METHOD_NOT_ALLOWED.value()));
    }

    @Test
    public void shouldReturn200WhenCancelReservation() throws Exception {
        mockMvc.perform(delete("/reservations/" + RESERVATION_ID)).andExpect(status().isOk());
    }

    @Test
    public void shouldReturn404WhenCancelInvalidReservationId() throws Exception {
        doThrow(new NoSuchElementException()).when(reservationService).cancel(RESERVATION_ID);

        mockMvc.perform(delete("/reservations/" + RESERVATION_ID)).andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void shouldReturnReservationJsonWhenFindById() throws Exception {
        Reservation reservation = ReservationServiceTest.getReservation();
        reservation.setId(RESERVATION_ID);
        doReturn(reservation).when(reservationService).getReservationById(RESERVATION_ID);

        mockMvc.perform(get("/reservations/" + RESERVATION_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(RESERVATION_ID.intValue())))
                .andExpect(jsonPath("$.name", is(reservation.getName())))
                .andExpect(jsonPath("$.email", is(reservation.getEmail())))
                .andExpect(jsonPath("$.arrivalDate", is(reservation.getArrivalDate().toString())))
                .andExpect(jsonPath("$.departureDate", is(reservation.getDepartureDate().toString())));
    }

    @Test
    public void shouldReturn404WhenFindInvalidReservationId() throws Exception {
        doThrow(new NoSuchElementException()).when(reservationService).getReservationById(99L);

        mockMvc.perform(get("/reservations/99"))
                .andExpect(status().is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    public void shouldReturnReservationsJsonWhenFindByRange() throws Exception {
        LocalDate from = of(2022, 8, 10);
        LocalDate to = of(2022, 9, 10);

        Reservation reservation = ReservationServiceTest.getReservation();
        reservation.setId(RESERVATION_ID);
        reservation.loadDays();

        Map<LocalDate, Reservation> reservedDays = reservation.getDays().stream()
                .collect(Collectors.toMap(ReservationDay::getReservationDay, ReservationDay::getReservation));

        doReturn(reservedDays).when(reservedDateService).getReservedDates(from, to);

        mockMvc.perform(get("/reservations?from=" + from + "&to=" + to))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(RESERVATION_ID.intValue())))
                .andExpect(jsonPath("$[0].name", is(reservation.getName())))
                .andExpect(jsonPath("$[0].email", is(reservation.getEmail())))
                .andExpect(jsonPath("$[0].arrivalDate", is(reservation.getArrivalDate().toString())))
                .andExpect(jsonPath("$[0].departureDate", is(reservation.getDepartureDate().toString())));
    }
}