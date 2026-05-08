package id.ac.ui.cs.advprog.jsonbackend.auth.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true, nullable = false, length = 64)
    private String username;

    private String fullName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Builder.Default
    private boolean isVerifiedJastiper = false;

    @Builder.Default
    private int successfulTransactions = 0;

    @Builder.Default
    private double rating = 0.0;

    @Column(name = "kyc_full_name")
    private String kycFullName;

    @Column(name = "identity_number", length = 50)
    private String identityNumber;

    @Column(name = "social_media_link")
    private String socialMediaLink;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}