package com.example.SpringMate.Listing.Entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "listing_conditions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_listing_condition_code", columnNames = "code")
})
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @Column(length = 500)
    private String description;

    @Builder.Default
    private boolean active = true;
}
