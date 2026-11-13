package se.lexicon.flightbooking_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import se.lexicon.flightbooking_api.dto.FlightLiveStatus;
import se.lexicon.flightbooking_api.dto.FlightStatusDTO;
import se.lexicon.flightbooking_api.entity.Flight;
import se.lexicon.flightbooking_api.repository.FlightRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/**
 * Live flight status (a demo simulation).
 * Every 20 seconds one flight gets a new delay, and every change
 * is sent to the open browsers with Server-Sent Events (SSE).
 */
@Service
@RequiredArgsConstructor
public class FlightStatusService {

    private static final int HOURS_AHEAD = 72; // live status for the next 3 days
    private static final long CONNECTION_TIMEOUT_MS = 30 * 60 * 1000L; // the browser reconnects by itself
    private static final int[] DELAY_CHOICES = {0, 0, 0, 15, 25, 40, 60}; // more zeros = mostly on time

    private final FlightRepository flightRepository;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>(); // the open browsers
    private final Map<Long, Integer> delays = new ConcurrentHashMap<>(); // flight id → delay in minutes
    private final Map<Long, FlightStatusDTO> lastSent = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public List<FlightStatusDTO> currentStatuses() {
        LocalDateTime now = LocalDateTime.now();
        return trackedFlights(now).stream()
                .map(flight -> statusOf(flight, now))
                .toList();
    }

    // A browser wants live updates: keep its connection open
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(CONNECTION_TIMEOUT_MS);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(emitter::complete);
        emitter.onError(error -> emitters.remove(emitter));
        emitters.add(emitter);

        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (Exception e) {
            emitters.remove(emitter);
        }
        return emitter;
    }

    @Scheduled(fixedRate = 20_000)
    public void updateStatuses() {
        LocalDateTime now = LocalDateTime.now();
        List<Flight> flights = trackedFlights(now);

        // 1. Simulation: one flight that leaves in more than 1 hour gets a new delay
        List<Flight> canChange = flights.stream()
                .filter(flight -> flight.getDepartureTime().isAfter(now.plusHours(1)))
                .toList();
        if (!canChange.isEmpty()) {
            Flight flight = canChange.get(random.nextInt(canChange.size()));
            delays.put(flight.getId(), DELAY_CHOICES[random.nextInt(DELAY_CHOICES.length)]);
        }

        // 2. Send every status that changed since last time
        for (Flight flight : flights) {
            FlightStatusDTO status = statusOf(flight, now);
            FlightStatusDTO previous = lastSent.put(flight.getId(), status);
            if (previous != null && !previous.equals(status)) {
                sendToAll(() -> SseEmitter.event().name("status").data(status));
            }
        }

        // 3. A small "ping", so the connection stays open
        sendToAll(() -> SseEmitter.event().comment("ping"));
    }

    // Each browser gets its own copy of the message
    private void sendToAll(Supplier<SseEmitter.SseEventBuilder> message) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(message.get());
            } catch (Exception e) {
                emitters.remove(emitter); // the browser is gone (for example the tab was closed)
            }
        }
    }

    private List<Flight> trackedFlights(LocalDateTime now) {
        LocalDateTime until = now.plusHours(HOURS_AHEAD);
        return flightRepository.findUpcoming(now.minusHours(6)).stream()
                .filter(flight -> flight.getDepartureTime().isBefore(until))
                .toList();
    }

    private FlightStatusDTO statusOf(Flight flight, LocalDateTime now) {
        int delay = delays.getOrDefault(flight.getId(), 0);
        LocalDateTime departure = flight.getDepartureTime().plusMinutes(delay);
        LocalDateTime arrival = flight.getArrivalTime().plusMinutes(delay);

        FlightLiveStatus status;
        if (now.isAfter(arrival)) {
            status = FlightLiveStatus.LANDED;
        } else if (now.isAfter(departure)) {
            status = FlightLiveStatus.DEPARTED;
        } else if (now.isAfter(departure.minusMinutes(40))) {
            status = FlightLiveStatus.BOARDING;
        } else if (delay > 0) {
            status = FlightLiveStatus.DELAYED;
        } else {
            status = FlightLiveStatus.ON_TIME;
        }

        return new FlightStatusDTO(flight.getId(), flight.getFlightNumber(), status.name(), delay, departure);
    }
}