package se.lexicon.flightbooking_api.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.entity.AppUser;
import se.lexicon.flightbooking_api.entity.Airport;
import se.lexicon.flightbooking_api.repository.AirportRepository;
import se.lexicon.flightbooking_api.repository.UserRepository;
import se.lexicon.flightbooking_api.service.DemoDataService;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkyBookDataSeeder implements CommandLineRunner {

    public static final String DEMO_PASSWORD = "SkyBookDemo2026";

    private final AirportRepository airportRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DemoDataService demoDataService;

    @Override
    public void run(String... args) {
        // 1. Airports and the demo user: only the first time
        if (airportRepository.count() == 0) {
            airportRepository.saveAll(List.of(
                    airport("ARN", "Stockholm Arlanda Airport", "Stockholm", "Sweden", 59.6519, 17.9186),
                    airport("LHR", "London Heathrow Airport", "London", "United Kingdom", 51.4700, -0.4543),
                    airport("CDG", "Paris Charles de Gaulle Airport", "Paris", "France", 49.0097, 2.5479),
                    airport("FCO", "Rome Fiumicino Airport", "Rome", "Italy", 41.8003, 12.2389),
                    airport("AMS", "Amsterdam Airport Schiphol", "Amsterdam", "Netherlands", 52.3105, 4.7683),
                    airport("BCN", "Barcelona El Prat Airport", "Barcelona", "Spain", 41.2974, 2.0833),
                    airport("BER", "Berlin Brandenburg Airport", "Berlin", "Germany", 52.3667, 13.5033),
                    airport("VIE", "Vienna International Airport", "Vienna", "Austria", 48.1103, 16.5697),
                    airport("MAD", "Madrid Barajas Airport", "Madrid", "Spain", 40.4983, -3.5676),
                    airport("PRG", "Prague Vaclav Havel Airport", "Prague", "Czech Republic", 50.1008, 14.2600),
                    airport("CPH", "Copenhagen Airport", "Copenhagen", "Denmark", 55.6180, 12.6508),
                    airport("OSL", "Oslo Gardermoen Airport", "Oslo", "Norway", 60.1976, 11.1004),
                    airport("HEL", "Helsinki Airport", "Helsinki", "Finland", 60.3172, 24.9633)
            ));

            userRepository.save(AppUser.builder()
                    .name("Demo Traveller")
                    .email(DemoDataService.DEMO_EMAIL)
                    .passwordHash(passwordEncoder.encode(DEMO_PASSWORD))
                    .createdAt(LocalDateTime.now())
                    .build());

            log.info("Seeded 13 airports and the demo user.");
        }

        // 2. Flights and demo bookings: every time the app starts
        demoDataService.topUpFlights();
        demoDataService.ensureDemoBookings();
    }

    private Airport airport(String code, String name, String city, String country,
                            double latitude, double longitude) {
        return Airport.builder()
                .code(code)
                .name(name)
                .city(city)
                .country(country)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}