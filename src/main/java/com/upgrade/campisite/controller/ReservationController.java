package com.upgrade.campisite.controller;

import com.upgrade.campisite.controller.mapper.ReservationMapper;
import com.upgrade.campisite.dto.ReservationDTO;
import com.upgrade.campisite.service.ReservationService;
import com.upgrade.campisite.service.ReservedDateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

	private ReservationService reservationService;
	private ReservationMapper reservationMapper;
	private ReservedDateService reservedDateService;

	public ReservationController(ReservationService reservationService,
								 ReservationMapper reservationMapper,
								 ReservedDateService reservedDateService) {
		this.reservationService = reservationService;
		this.reservationMapper = reservationMapper;
		this.reservedDateService = reservedDateService;
	}

	@Operation(summary = "Book campsite")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Successfully booked", content = @Content),
			@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content)
	})
	@PostMapping
	@ResponseBody
	@ResponseStatus(HttpStatus.CREATED)
	public long book(@Valid @RequestBody ReservationDTO reservationDTO) {
		return reservationService.book(reservationMapper.toModel(reservationDTO));
	}

	@Operation(summary = "Modify campsite reservation by id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully modified", content = @Content),
			@ApiResponse(responseCode = "400", description = "Invalid request", content = @Content),
			@ApiResponse(responseCode = "404", description = "No reservation found", content = @Content),
			@ApiResponse(responseCode = "405", description = "Not able to modify canceled reservation", content = @Content)
	})
	@PutMapping(value = "/{reservationId}")
	@ResponseStatus(HttpStatus.OK)
	public void modify(@PathVariable long reservationId, @Valid @RequestBody ReservationDTO reservationDTO) {
		reservationService.modify(reservationId, reservationMapper.toModel(reservationDTO));
	}

	@Operation(summary = "Cancel campsite reservation by id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully canceled", content = @Content),
			@ApiResponse(responseCode = "404", description = "No reservation found", content = @Content),
	})
	@DeleteMapping(value = "/{reservationId}")
	@ResponseStatus(HttpStatus.OK)
	public void cancel(@PathVariable long reservationId) {
		reservationService.cancel(reservationId);
	}

	@Operation(summary = "Find reservation by id")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully found reservation",
						 content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ReservationDTO.class)) }),
			@ApiResponse(responseCode = "404", description = "No reservation found", content = @Content),
	})
	@GetMapping(value = "/{reservationId}")
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public ReservationDTO getReservationById(@PathVariable long reservationId) {
		return reservationMapper.toDTO(reservationService.getReservationById(reservationId));
	}

	@Operation(summary = "Find reservations by range")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully found reservations",
					content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReservationDTO.class))) })
	})
	@GetMapping
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public List<ReservationDTO> getReservations(@RequestParam @DateTimeFormat(iso=DATE) LocalDate from,
											    @RequestParam @DateTimeFormat(iso=DATE) LocalDate to) {
		return new HashSet<>(reservedDateService.getReservedDates(from, to).values()).stream()
				.map(reservationMapper::toDTO)
				.collect(Collectors.toList());
	}
}