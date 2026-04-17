package se.lexicon.flightbooking_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FlightChatbotAssistantService {
    private final ChatClient chatClient;
    private final FlightBookingService flightBookingService;

    public String handleMessage(String message) {
        String lower=message.toLowerCase();

        // 1. Show available flights
        if(lower.contains("available")){
            return flightBookingService.findAvailableFlights().toString();

        }
        // 2. Book flight (simple guidance)
        if(lower.contains("booking")){
            return "To book a flight, please provide: flightId, name, email";
        }
        // 3. Cancel flight
        if(lower.contains("cancel")){
            return "To cancel a flight, please provide: flightId,and email";
        }
        // 4. Default → AI response
        String systemPrompt = """
                You are a helpful flight booking assistant.

                You can:
                - Show available flights
                - Help users book flights
                - Help cancel bookings

                Keep answers short and simple.
                Ask for missing information if needed.
                """;
        return chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .call()
                .content();

    }
}
