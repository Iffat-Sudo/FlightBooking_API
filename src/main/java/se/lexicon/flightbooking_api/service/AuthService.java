package se.lexicon.flightbooking_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.flightbooking_api.dto.AuthResponseDTO;
import se.lexicon.flightbooking_api.dto.LoginRequestDTO;
import se.lexicon.flightbooking_api.dto.RegisterRequestDTO;
import se.lexicon.flightbooking_api.dto.UserDTO;
import se.lexicon.flightbooking_api.entity.AppUser;
import se.lexicon.flightbooking_api.exception.EmailAlreadyUsedException;
import se.lexicon.flightbooking_api.exception.InvalidCredentialsException;
import se.lexicon.flightbooking_api.exception.ResourceNotFoundException;
import se.lexicon.flightbooking_api.repository.UserRepository;
import se.lexicon.flightbooking_api.security.TokenService;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthResponseDTO register(RegisterRequestDTO request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyUsedException("This email is already registered");
        }

        AppUser user = userRepository.save(AppUser.builder()
                .name(request.name().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .createdAt(LocalDateTime.now())
                .build());

        return tokenService.createToken(user);
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO request) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new InvalidCredentialsException("Wrong email or password"));

        return tokenService.createToken(user);
    }

    @Transactional(readOnly = true)
    public UserDTO findCurrentUser(String email) {
        AppUser user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserDTO(user.getId(), user.getName(), user.getEmail());
    }
}