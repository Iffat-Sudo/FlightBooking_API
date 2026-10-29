package se.lexicon.flightbooking_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.flightbooking_api.dto.AirportDTO;
import se.lexicon.flightbooking_api.dto.BookFlightRequestDTO;
import se.lexicon.flightbooking_api.dto.BookingDTO;
import se.lexicon.flightbooking_api.dto.FlightDTO;
import se.lexicon.flightbooking_api.entity.Booking;
import se.lexicon.flightbooking_api.entity.BookingStatus;
import se.lexicon.flightbooking_api.entity.Flight;
import se.lexicon.flightbooking_api.exception.FlightBookingException;
import se.lexicon.flightbooking_api.exception.ResourceNotFoundException;
import se.lexicon.flightbooking_api.mapper.FlightBookingMapper;
import se.lexicon.flightbooking_api.repository.AirportRepository;
import se.lexicon.flightbooking_api.repository.BookingRepository;
import se.lexicon.flightbooking_api.repository.FlightRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class FlightBookingServiceImpl implements FlightBookingService {

    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;
    private final BookingRepository bookingRepository;
    private final FlightBookingMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<AirportDTO> findAllAirports() {
        return airportRepository.findAll(Sort.by("city")).stream()
                .map(mapper::toAirportDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightDTO> findUpcomingFlights() {
        return flightRepository.findUpcoming(LocalDateTime.now()).stream()
                .map(mapper::toFlightDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightDTO> findAvailableFlights() {
        return searchFlights(null, null, null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FlightDTO> searchFlights(String from, String to, LocalDate date, BigDecimal maxPrice) {
        LocalDateTime now = LocalDateTime.now();

        // No date: from now until 1 year ahead. With a date: only that day.
        LocalDateTime start = (date == null) ? now : date.atStartOfDay();
        LocalDateTime end = (date == null) ? now.plusYears(1) : date.plusDays(1).atStartOfDay();
        if (start.isBefore(now)) {
            start = now; // never show flights that already left
        }

        return flightRepository.search(toCode(from), toCode(to), start, end, maxPrice).stream()
                .map(mapper::toFlightDTO)
                .toList();
    }

    @Override
    public BookingDTO bookFlight(Long flightId, BookFlightRequestDTO request) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        if (flight.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new FlightBookingException("This flight has already departed");
        }
        if (flight.getAvailableSeats() <= 0) {
            throw new FlightBookingException("This flight is fully booked");
        }

        flight.setAvailableSeats(flight.getAvailableSeats() - 1);

        Booking booking = bookingRepository.save(Booking.builder()
                .flight(flight)
                .passengerName(request.passengerName())
                .passengerEmail(request.passengerEmail())
                .status(BookingStatus.CONFIRMED)
                .bookedAt(LocalDateTime.now())
                .build());

        return mapper.toBookingDTO(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDTO> findBookingsByEmail(String email) {
        return bookingRepository.findByPassengerEmailWithFlight(email).stream()
                .map(mapper::toBookingDTO)
                .toList();
    }

    @Override
    public void cancelBooking(Long bookingId, String email) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!booking.getPassengerEmail().equalsIgnoreCase(email)) {
            throw new FlightBookingException("Passenger email does not match");
        }
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new FlightBookingException("This booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        // Give the seat back to the flight
        Flight flight = booking.getFlight();
        flight.setAvailableSeats(flight.getAvailableSeats() + 1);
    }

    // "arn " → "ARN", and empty text → null (= no filter)
    private String toCode(String code) {
        return (code == null || code.isBlank()) ? null : code.trim().toUpperCase();
    }
}