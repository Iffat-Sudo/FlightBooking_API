package se.lexicon.flightbooking_api.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.flightbooking_api.entity.Airport;
import se.lexicon.flightbooking_api.entity.Booking;
import se.lexicon.flightbooking_api.entity.BookingStatus;
import se.lexicon.flightbooking_api.entity.Flight;
import se.lexicon.flightbooking_api.repository.AirportRepository;
import se.lexicon.flightbooking_api.repository.BookingRepository;
import se.lexicon.flightbooking_api.repository.FlightRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class SkyBookDataSeeder implements CommandLineRunner {

    private static final int DAYS = 30;
    private static final int FLIGHTS_PER_DAY = 3;
    private static final int SEATS = 120;

    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // Only add test data if the database is empty
        if (airportRepository.count() > 0) {
            log.info("Test data already exists. Skipping the seeder.");
            return;
        }

        List<Airport> airports = airportRepository.saveAll(List.of(
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

        Airport stockholm = airports.get(0);
        List<Airport> others = airports.subList(1, airports.size());

        // A fixed "seed" number gives the same test data every time
        Random random = new Random(42);
        LocalDate today = LocalDate.now();
        List<Flight> flights = new ArrayList<>();
        int flightNumber = 101;

        for (int day = 1; day <= DAYS; day++) {
            LocalDate date = today.plusDays(day);

            for (int i = 0; i < FLIGHTS_PER_DAY; i++) {
                Airport other = others.get(random.nextInt(others.size()));
                // 2 flights from Stockholm, 1 flight back to Stockholm
                boolean fromStockholm = i < 2;
                Airport from = fromStockholm ? stockholm : other;
                Airport to = fromStockholm ? other : stockholm;

                double km = distanceKm(from, to);
                int minutes = roundTo5(30 + km / 800 * 60); // 800 km/h + 30 min on the ground
                LocalDateTime departure = date.atTime(6 + random.nextInt(15), random.nextBoolean() ? 0 : 30);

                flights.add(Flight.builder()
                        .flightNumber("SB" + flightNumber++)
                        .departureAirport(from)
                        .arrivalAirport(to)
                        .departureTime(departure)
                        .arrivalTime(departure.plusMinutes(minutes))
                        .price(price(km, random))
                        .totalSeats(SEATS)
                        .availableSeats(SEATS)
                        .build());
            }
        }
        flightRepository.saveAll(flights);

        // A few example bookings
        List<Booking> bookings = List.of(
                booking(flights.get(0), "John Doe", "john.doe@example.com"),
                booking(flights.get(1), "Jane Smith", "jane.smith@example.com"),
                booking(flights.get(4), "Mike Johnson", "mike.johnson@example.com")
        );
        bookingRepository.saveAll(bookings);

        log.info("Seeded {} airports, {} flights and {} bookings.",
                airports.size(), flights.size(), bookings.size());
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

    private Booking booking(Flight flight, String name, String email) {
        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        return Booking.builder()
                .flight(flight)
                .passengerName(name)
                .passengerEmail(email)
                .status(BookingStatus.CONFIRMED)
                .bookedAt(LocalDateTime.now())
                .build();
    }

    // Price: a base price + a price per km + a little random difference, ending with .99
    private BigDecimal price(double km, Random random) {
        long rounded = Math.round(49 + km * 0.12 + random.nextInt(80));
        return BigDecimal.valueOf(rounded).subtract(new BigDecimal("0.01"));
    }

    private int roundTo5(double minutes) {
        return (int) (Math.round(minutes / 5) * 5);
    }

    // The "haversine" formula: the real distance between two points on Earth
    private double distanceKm(Airport a, Airport b) {
        double lat1 = Math.toRadians(a.getLatitude());
        double lat2 = Math.toRadians(b.getLatitude());
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(b.getLongitude() - a.getLongitude());
        double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * 6371 * Math.asin(Math.sqrt(h));
    }
}