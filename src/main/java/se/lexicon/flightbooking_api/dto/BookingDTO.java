package se.lexicon.flightbooking_api.dto;

import java.time.LocalDateTime;

public record BookingDTO(
        Long id,
        FlightDTO flight,
        String passengerName,
        String passengerEmail,
        String status,
        LocalDateTime bookedAt
) {}