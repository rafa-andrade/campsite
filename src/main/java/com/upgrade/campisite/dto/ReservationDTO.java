package com.upgrade.campisite.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class ReservationDTO {

    private Long id;

    @NotBlank
    private String name;

    @Email
    @NotNull
    private String email;

    @NotNull
    private LocalDate arrivalDate;

    @NotNull
    private LocalDate departureDate;
}