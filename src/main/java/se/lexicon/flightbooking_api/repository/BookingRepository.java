package se.lexicon.flightbooking_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.lexicon.flightbooking_api.entity.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("""
            select b from Booking b
            join fetch b.flight f
            join fetch f.departureAirport
            join fetch f.arrivalAirport
            where lower(b.passengerEmail) = lower(:email)
            order by f.departureTime
            """)
    List<Booking> findByPassengerEmailWithFlight(@Param("email") String email);
}