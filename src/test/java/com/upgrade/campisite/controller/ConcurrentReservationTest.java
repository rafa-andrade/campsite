package com.upgrade.campisite.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.upgrade.campisite.model.Reservation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ConcurrentReservationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    protected MockMvc mockMvc;

    @Test
    public void test() throws Exception {
        //10 days range
        LocalDate firstReservationDay = LocalDate.now().plusDays(2);
        LocalDate lastReservationDay = LocalDate.now().plusDays(11);

        IntStream.range(1, 30).boxed().parallel().forEach(threadId ->
            Stream.iterate(firstReservationDay, date -> date.plusDays(1))
                    .limit(DAYS.between(firstReservationDay, lastReservationDay) + 1)
                    .forEach(date -> {
                        Reservation reservation = new Reservation();
                        reservation.setName("Thread " + threadId);
                        reservation.setEmail("test" + threadId + "@gmail.com");
                        reservation.setArrivalDate(date);
                        reservation.setDepartureDate(date);

                        try {
                            mockMvc.perform(post("/reservations")
                                            .content(objectMapper.writeValueAsString(reservation))
                                            .contentType(APPLICATION_JSON));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    })
        );

        mockMvc.perform(get("/reservations?from=" + firstReservationDay + "&to=" + lastReservationDay))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(10)));
    }
}
