package com.upgrade.campisite.repository;

import com.upgrade.campisite.model.Reservation;
import com.upgrade.campisite.model.ReservationDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query(value = "select d from " +
            "ReservationDay d " +
            "join d.reservation r " +
            "where d.reservationDay between :from and :to " +
            "and r.cancellationDate is null")
    List<ReservationDay> getReservationDays(@Param("from") LocalDate from, @Param("to") LocalDate to);

}