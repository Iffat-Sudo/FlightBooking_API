package se.lexicon.flightbooking_api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 3)
    private String code; // e.g. "ARN"

    @Column(nullable = false)
    private String name; // e.g. "Stockholm Arlanda Airport"

    @Column(nullable = false)
    private String city; // e.g. "Stockholm"

    @Column(nullable = false)
    private String country; // e.g. "Sweden"

    @Column(nullable = false)
    private Double latitude; // for the map in Phase 3

    @Column(nullable = false)
    private Double longitude;
}