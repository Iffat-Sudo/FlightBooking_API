package se.lexicon.flightbooking_api.dto;

import java.time.Instant;

public record AuthResponseDTO(String token, Instant expiresAt, UserDTO user) {}