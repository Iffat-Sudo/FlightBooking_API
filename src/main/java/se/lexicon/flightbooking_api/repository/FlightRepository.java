package se.lexicon.flightbooking_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.lexicon.flightbooking_api.entity.Flight;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("""
            select f from Flight f
            join fetch f.departureAirport
            join fetch f.arrivalAirport
            where f.departureTime > :now
            order by f.departureTime
            """)
    List<Flight> findUpcoming(@Param("now") LocalDateTime now);

    @Query("""
            select f from Flight f
            join fetch f.departureAirport d
            join fetch f.arrivalAirport a
            where (:from is null or d.code = :from)
              and (:to is null or a.code = :to)
              and f.departureTime >= :start
              and f.departureTime < :end
              and (:maxPrice is null or f.price <= :maxPrice)
              and f.availableSeats > 0
            order by f.departureTime
            """)
    List<Flight> search(@Param("from") String from,
                        @Param("to") String to,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("maxPrice") BigDecimal maxPrice);
}