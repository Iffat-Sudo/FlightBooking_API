package se.lexicon.flightbooking_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import se.lexicon.flightbooking_api.dto.FlightStatusDTO;
import se.lexicon.flightbooking_api.service.FlightStatusService;

import java.util.List;

@RestController
@RequestMapping("/api/flights/status")
@RequiredArgsConstructor
@Tag(name = "Live flight status", description = "Status of flights in the next 3 days (demo simulation)")
public class FlightStatusController {

    private final FlightStatusService flightStatusService;

    @Operation(summary = "Current status", description = "Status of all flights in the next 3 days")
    @GetMapping
    public ResponseEntity<List<FlightStatusDTO>> getStatuses() {
        return ResponseEntity.ok(flightStatusService.currentStatuses());
    }

    @Operation(summary = "Live updates", description = "Server-Sent Events: the server sends a status when it changes")
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamStatuses() {
        return flightStatusService.subscribe();
    }
}