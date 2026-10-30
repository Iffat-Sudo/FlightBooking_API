package se.lexicon.flightbooking_api.service;

import se.lexicon.flightbooking_api.dto.AirportDTO;
import se.lexicon.flightbooking_api.dto.BookFlightRequestDTO;
import se.lexicon.flightbooking_api.dto.BookingDTO;
import se.lexicon.flightbooking_api.dto.FlightDTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface FlightBookingService {

    List<AirportDTO> findAllAirports();

    List<FlightDTO> findUpcomingFlights();

    List<FlightDTO> findAvailableFlights();

    List<FlightDTO> searchFlights(String from, String to, LocalDate date, BigDecimal maxPrice);

    BookingDTO bookFlight(Long flightId, BookFlightRequestDTO request, String userEmail);

    List<BookingDTO> findMyBookings(String userEmail);

    void cancelBooking(Long bookingId, String userEmail);
}