package com.upgrade.campisite.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoUnit.DAYS;

@Entity
@Data
@EqualsAndHashCode(of = "id")
public class Reservation implements Serializable {
    @Id
    @GeneratedValue
    private long id;

    private String name;

    private String email;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "reservation")
    private List<ReservationDay> days = new ArrayList<>();

    public void loadDays() {
        days = Stream.iterate(arrivalDate, day -> day.plusDays(1))
                .limit(DAYS.between(arrivalDate, departureDate) + 1)
                .map(day -> new ReservationDay(null, this, day))
                .collect(Collectors.toList());
    }

    private LocalDate arrivalDate;

    private LocalDate departureDate;

    private LocalDateTime bookingDate;

    private LocalDateTime modificationDate;

    private LocalDateTime cancellationDate;
}