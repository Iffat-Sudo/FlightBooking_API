package se.lexicon.flightbooking_api.dto;

public record AirportDTO(
        Long id,
        String code,
        String name,
        String city,
        String country,
        Double latitude,
        Double longitude
) {}