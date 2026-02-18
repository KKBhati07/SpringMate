package com.example.SpringMate.Listing.Entity;

import com.example.SpringMate.User.Entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "contact_messages",
        indexes = {
                @Index(name = "idx_contact_messages_listing_id", columnList = "listing_id"),
                @Index(name = "idx_contact_messages_seller_id", columnList = "seller_id"),
                @Index(name = "idx_contact_messages_buyer_id", columnList = "buyer_id"),
                @Index(name = "idx_contact_messages_status", columnList = "status")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "listing_id", nullable = false)
    private Long listingId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", insertable = false, updatable = false)
    private Listing listing;

    @Column(name = "seller_id", nullable = false)
    private Long sellerId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", insertable = false, updatable = false)
    private User seller;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", insertable = false, updatable = false)
    private User buyer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ContactMessageStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "subject_length")
    private Integer subjectLength;

    @Column(name = "body_length")
    private Integer bodyLength;
}

