package se.lexicon.flightbooking_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.flightbooking_api.entity.AppUser;
import se.lexicon.flightbooking_api.entity.Airport;
import se.lexicon.flightbooking_api.entity.Booking;
import se.lexicon.flightbooking_api.entity.BookingStatus;
import se.lexicon.flightbooking_api.entity.Flight;
import se.lexicon.flightbooking_api.repository.AirportRepository;
import se.lexicon.flightbooking_api.repository.BookingRepository;
import se.lexicon.flightbooking_api.repository.FlightRepository;
import se.lexicon.flightbooking_api.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemoDataService {

    public static final String DEMO_EMAIL = "demo@skybook.com";

    private static final int DAYS_AHEAD = 30;
    private static final int FLIGHTS_PER_DAY = 3;
    private static final int SEATS = 120;

    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    /**
     * Adds new flights, so there are always flights for the next 30 days.
     * Returns how many flights were added.
     */
    @Transactional
    public int topUpFlights() {
        Airport stockholm = airportRepository.findByCode("ARN").orElse(null);
        if (stockholm == null) {
            return 0; // no airports yet
        }
        List<Airport> others = airportRepository.findAll().stream()
                .filter(airport -> !airport.getCode().equals("ARN"))
                .toList();

        LocalDate today = LocalDate.now();
        // Start the day after the latest flight (but never in the past)
        LocalDate firstDay = flightRepository.findLatestDepartureTime()
                .map(latest -> latest.toLocalDate().plusDays(1))
                .filter(day -> day.isAfter(today))
                .orElse(today.plusDays(1));
        LocalDate lastDay = today.plusDays(DAYS_AHEAD);

        List<Flight> flights = new ArrayList<>();
        long flightNumber = flightRepository.count() + 101;

        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            // The same date always gives the same flights
            Random random = new Random(date.toEpochDay());

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
        if (!flights.isEmpty()) {
            log.info("Added {} new flights ({} to {}).", flights.size(), firstDay, lastDay);
        }
        return flights.size();
    }

    /**
     * Makes sure the demo account always has upcoming bookings,
     * so visitors see a full "My bookings" page.
     */
    @Transactional
    public void ensureDemoBookings() {
        AppUser demo = userRepository.findByEmailIgnoreCase(DEMO_EMAIL).orElse(null);
        if (demo == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean hasUpcoming = bookingRepository.findByUserEmailWithFlight(DEMO_EMAIL).stream()
                .anyMatch(booking -> booking.getStatus() == BookingStatus.CONFIRMED
                        && booking.getFlight().getDepartureTime().isAfter(now));
        if (hasUpcoming) {
            return;
        }

        List<Flight> upcoming = flightRepository.findUpcoming(now);
        if (upcoming.size() < 8) {
            return;
        }

        List<Booking> bookings = List.of(
                booking(upcoming.get(2), demo, "Demo Traveller", DEMO_EMAIL),
                booking(upcoming.get(7), demo, "Alex Traveller", "alex@example.com")
        );
        bookingRepository.saveAll(bookings);
        log.info("Added {} new demo bookings.", bookings.size());
    }

    private Booking booking(Flight flight, AppUser user, String passengerName, String passengerEmail) {
        flight.setAvailableSeats(flight.getAvailableSeats() - 1);
        return Booking.builder()
                .flight(flight)
                .user(user)
                .passengerName(passengerName)
                .passengerEmail(passengerEmail)
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
