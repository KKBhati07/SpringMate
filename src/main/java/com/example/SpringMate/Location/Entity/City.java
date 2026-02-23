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
        name = "cities",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "Unique_cities",
                        columnNames = {"name", "state_id"}
                )
        }
)
public class City {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @NonNull
    private String name;

    @ManyToOne
    @JoinColumn(name = "state_id", nullable = false)
    @NonNull
    private State state;
}
