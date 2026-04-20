package se.lexicon.flightbooking_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.lexicon.flightbooking_api.dto.AIRequestDTO;
import se.lexicon.flightbooking_api.dto.AIResponseDTO;
import se.lexicon.flightbooking_api.service.FlightChatbotAssistantService;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor

public class FlightChatbotController {
    private final FlightChatbotAssistantService flightChatbotAssistantService;

    @PostMapping

    public AIResponseDTO chat(@RequestBody AIRequestDTO aiRequestDTO) {

        String response = flightChatbotAssistantService
                .handleMessage(aiRequestDTO.message());

        return new AIResponseDTO(response);
    }
}
