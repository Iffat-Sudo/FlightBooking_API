package se.lexicon.flightbooking_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import se.lexicon.flightbooking_api.dto.AirportDTO;
import se.lexicon.flightbooking_api.dto.BookFlightRequestDTO;
import se.lexicon.flightbooking_api.dto.BookingDTO;
import se.lexicon.flightbooking_api.dto.FlightDTO;
import se.lexicon.flightbooking_api.service.FlightBookingService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Flight Booking API", description = "Airports, flights and bookings")
public class FlightBookingController {

    private final FlightBookingService flightBookingService;

    @Operation(summary = "Get all airports", description = "Returns all airports, sorted by city")
    @GetMapping("/airports")
    public ResponseEntity<List<AirportDTO>> getAirports() {
        return ResponseEntity.ok(flightBookingService.findAllAirports());
    }

    @Operation(summary = "Get upcoming flights", description = "Returns all flights in the future")
    @GetMapping("/flights")
    public ResponseEntity<List<FlightDTO>> getUpcomingFlights() {
        return ResponseEntity.ok(flightBookingService.findUpcomingFlights());
    }

    @Operation(summary = "Get available flights", description = "Returns future flights with free seats")
    @GetMapping("/flights/available")
    public ResponseEntity<List<FlightDTO>> getAvailableFlights() {
        return ResponseEntity.ok(flightBookingService.findAvailableFlights());
    }

    @Operation(summary = "Search flights", description = "All filters are optional")
    @GetMapping("/flights/search")
    public ResponseEntity<List<FlightDTO>> searchFlights(
            @Parameter(description = "Departure airport code, e.g. ARN") @RequestParam(required = false) String from,
            @Parameter(description = "Arrival airport code, e.g. LHR") @RequestParam(required = false) String to,
            @Parameter(description = "Departure date, e.g. 2026-11-12")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Highest price, e.g. 300") @RequestParam(required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(flightBookingService.searchFlights(from, to, date, maxPrice));
    }

    @Operation(summary = "Book a flight", description = "Books one seat. Needs login.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Flight successfully booked"),
            @ApiResponse(responseCode = "401", description = "Not logged in", content = @Content),
            @ApiResponse(responseCode = "404", description = "Flight not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Flight full or already departed", content = @Content)
    })
    @PostMapping("/flights/{flightId}/book")
    public ResponseEntity<BookingDTO> bookFlight(
            @Parameter(description = "ID of the flight to book") @PathVariable Long flightId,
            @Valid @RequestBody BookFlightRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(flightBookingService.bookFlight(flightId, request, jwt.getSubject()));
    }

    @Operation(summary = "My bookings", description = "Returns the bookings of the logged-in user",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingDTO>> getMyBookings(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(flightBookingService.findMyBookings(jwt.getSubject()));
    }

    @Operation(summary = "Cancel a booking", description = "Cancels one of your bookings and gives the seat back",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Booking successfully cancelled"),
            @ApiResponse(responseCode = "401", description = "Not logged in", content = @Content),
            @ApiResponse(responseCode = "404", description = "Booking not found", content = @Content),
            @ApiResponse(responseCode = "400", description = "Already cancelled", content = @Content)
    })
    @DeleteMapping("/bookings/{bookingId}")
    public ResponseEntity<Void> cancelBooking(
            @Parameter(description = "ID of the booking to cancel") @PathVariable Long bookingId,
            @AuthenticationPrincipal Jwt jwt) {
        flightBookingService.cancelBooking(bookingId, jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}