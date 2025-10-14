package com.example.SpringMate.Location.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Builder
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
    private Long id;

    @NonNull
    private String name;

    @ManyToOne
    @NonNull
    @JoinColumn(name = "country_id")
    private Country country;
}
