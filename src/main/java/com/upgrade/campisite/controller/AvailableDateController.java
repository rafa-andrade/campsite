package com.upgrade.campisite.controller;

import com.upgrade.campisite.dto.AvailableDateDTO;
import com.upgrade.campisite.dto.ReservationDTO;
import com.upgrade.campisite.service.AvailableDateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDate.now;
import static java.util.Optional.ofNullable;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE;

@Controller
@RequestMapping("/available-dates")
public class AvailableDateController {

	private AvailableDateService availableDateService;
	private int defaultRangeMonths;

	public AvailableDateController(AvailableDateService availableDateService,
								   @Value("${reservation.default-range-months-search}") int defaultRangeMonths) {
		this.availableDateService = availableDateService;
		this.defaultRangeMonths = defaultRangeMonths;
	}

	@Operation(summary = "Find reservations by range")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Successfully found reservations",
					content = {@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AvailableDateDTO.class))) })
	})
	@GetMapping
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public List<AvailableDateDTO> getAvailableDates(
			@RequestParam(required=false) @DateTimeFormat(iso=DATE) LocalDate from,
			@RequestParam(required=false) @DateTimeFormat(iso=DATE) LocalDate to) {
		from = ofNullable(from).orElse(now());
		to = ofNullable(to).orElse(from.plusMonths(defaultRangeMonths));
		return availableDateService.getAvailableDates(from, to);
	}
}