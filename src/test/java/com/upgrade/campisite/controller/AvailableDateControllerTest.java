package com.upgrade.campisite.controller;

import com.upgrade.campisite.dto.AvailableDateDTO;
import com.upgrade.campisite.service.AvailableDateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static java.time.LocalDate.of;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doReturn;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AvailableDateControllerTest {

    @MockBean
    private AvailableDateService availableDateService;

    @Autowired
    protected MockMvc mockMvc;

    @Test
    public void shouldReturnAvailableDates() throws Exception {
        LocalDate from = of(2022, 8, 1);
        LocalDate to = of(2022, 8, 3);

        List<AvailableDateDTO> availableDates = Arrays.asList(
                new AvailableDateDTO(of(2022, 8, 1)),
                new AvailableDateDTO(of(2022, 8, 2)));

        doReturn(availableDates).when(availableDateService).getAvailableDates(from, to);

        mockMvc.perform(get("/available-dates?from=" + from + "&to=" + to))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].availableDate", is("2022-08-01")))
                .andExpect(jsonPath("$[1].availableDate", is("2022-08-02")));
    }

    @Test
    public void shouldReturnAvailableDatesForDefaultRange() throws Exception {
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusMonths(1);

        List<AvailableDateDTO> availableDates = Arrays.asList(
                new AvailableDateDTO(of(2022, 8, 1)),
                new AvailableDateDTO(of(2022, 8, 2)));

        doReturn(availableDates).when(availableDateService).getAvailableDates(from, to);

        mockMvc.perform(get("/available-dates"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].availableDate", is("2022-08-01")))
                .andExpect(jsonPath("$[1].availableDate", is("2022-08-02")));
    }
}
