package se.lexicon.flightbooking_api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import se.lexicon.flightbooking_api.service.DemoDataService;

@Component
@RequiredArgsConstructor
public class DemoDataScheduler {

    private final DemoDataService demoDataService;

    // Every night at 03:00 ("second minute hour day month weekday")
    @Scheduled(cron = "0 0 3 * * *")
    public void refreshDemoData() {
        demoDataService.topUpFlights();
        demoDataService.ensureDemoBookings();
    }
}