package se.lexicon.flightbooking_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FlightBookingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlightBookingApiApplication.class, args);
    }

}
