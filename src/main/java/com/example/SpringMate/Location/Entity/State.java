package com.example.SpringMate.Location.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
@Table(
        name = "states",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "Unique_states",
                        columnNames = {"name", "country_id"}
                )
        }
)
public class State {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @NonNull
    private String name;

    @NonNull
    @Column(nullable = false)
    private String iso2;

    @ManyToOne
    @NonNull
    @JoinColumn(name = "country_id")
    private Country country;
}
