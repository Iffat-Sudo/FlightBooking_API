package se.lexicon.flightbooking_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.lexicon.flightbooking_api.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    // We add more methods in a later step
}