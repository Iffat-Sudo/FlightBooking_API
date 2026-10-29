package se.lexicon.flightbooking_api.mapper;

import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.dto.AirportDTO;
import se.lexicon.flightbooking_api.dto.BookingDTO;
import se.lexicon.flightbooking_api.dto.FlightDTO;
import se.lexicon.flightbooking_api.entity.Airport;
import se.lexicon.flightbooking_api.entity.Booking;
import se.lexicon.flightbooking_api.entity.Flight;

@Component
public class FlightBookingMapper {

    public AirportDTO toAirportDTO(Airport airport) {
        return new AirportDTO(
                airport.getId(),
                airport.getCode(),
                airport.getName(),
                airport.getCity(),
                airport.getCountry(),
                airport.getLatitude(),
                airport.getLongitude()
        );
    }

    public FlightDTO toFlightDTO(Flight flight) {
        return new FlightDTO(
                flight.getId(),
                flight.getFlightNumber(),
                toAirportDTO(flight.getDepartureAirport()),
                toAirportDTO(flight.getArrivalAirport()),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getPrice(),
                flight.getAvailableSeats(),
                flight.getTotalSeats()
        );
    }

    public BookingDTO toBookingDTO(Booking booking) {
        return new BookingDTO(
                booking.getId(),
                toFlightDTO(booking.getFlight()),
                booking.getPassengerName(),
                booking.getPassengerEmail(),
                booking.getStatus().name(),
                booking.getBookedAt()
        );
    }
}