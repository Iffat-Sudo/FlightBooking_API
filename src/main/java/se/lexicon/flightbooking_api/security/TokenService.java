package se.lexicon.flightbooking_api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import se.lexicon.flightbooking_api.dto.AuthResponseDTO;
import se.lexicon.flightbooking_api.dto.UserDTO;
import se.lexicon.flightbooking_api.entity.AppUser;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenService {

    private static final Duration TOKEN_VALIDITY = Duration.ofHours(24);

    private final JwtEncoder jwtEncoder;

    public AuthResponseDTO createToken(AppUser user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(TOKEN_VALIDITY);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("skybook-api")
                .subject(user.getEmail()) // "who is this token for?"
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("name", user.getName())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new AuthResponseDTO(token, expiresAt,
                new UserDTO(user.getId(), user.getName(), user.getEmail()));
    }
}