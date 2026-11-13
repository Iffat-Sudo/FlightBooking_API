package se.lexicon.flightbooking_api.dto;

import java.time.LocalDateTime;

public record FlightStatusDTO(
        Long flightId,
        String flightNumber,
        String status,
        Integer delayMinutes,
        LocalDateTime expectedDeparture
) {}
