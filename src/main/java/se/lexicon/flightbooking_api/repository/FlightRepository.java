package se.lexicon.flightbooking_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.lexicon.flightbooking_api.entity.Flight;

public interface FlightRepository extends JpaRepository<Flight, Long> {
    // We add the search query in a later step
}