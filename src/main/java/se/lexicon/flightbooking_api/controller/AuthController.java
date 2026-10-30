
package se.lexicon.flightbooking_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import se.lexicon.flightbooking_api.dto.AuthResponseDTO;
import se.lexicon.flightbooking_api.dto.LoginRequestDTO;
import se.lexicon.flightbooking_api.dto.RegisterRequestDTO;
import se.lexicon.flightbooking_api.dto.UserDTO;
import se.lexicon.flightbooking_api.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, log in and get the current user")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user", description = "Creates a user and returns a JWT token")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Log in", description = "Returns a JWT token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Get the logged-in user", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(authService.findCurrentUser(jwt.getSubject()));
    }
}