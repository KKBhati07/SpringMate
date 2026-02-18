package com.example.SpringMate.Location.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(
        name = "locations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "Unique_Location",
                        columnNames = {"city_id", "state_id", "country_id"}
                )
        }
)
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @ManyToOne(optional = false)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    @ManyToOne(optional = false) // For JPA level check not DB level
    @JoinColumn(name = "country_id", nullable = false)
    private Country country;
}
