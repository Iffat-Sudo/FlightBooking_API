package se.lexicon.flightbooking_api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FlightDTO(
        Long id,
        String flightNumber,
        AirportDTO departureAirport,
        AirportDTO arrivalAirport,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        BigDecimal price,
        Integer availableSeats,
        Integer totalSeats
) {}