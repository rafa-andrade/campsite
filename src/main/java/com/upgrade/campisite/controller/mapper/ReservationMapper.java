package com.upgrade.campisite.controller.mapper;

import com.upgrade.campisite.dto.ReservationDTO;
import com.upgrade.campisite.model.Reservation;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    private ModelMapper modelMapper;

    public ReservationMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Reservation toModel(ReservationDTO reservationDTO) {
        return modelMapper.map(reservationDTO, Reservation.class);
    }

    public ReservationDTO toDTO(Reservation reservation) {
        return modelMapper.map(reservation, ReservationDTO.class);
    }
}