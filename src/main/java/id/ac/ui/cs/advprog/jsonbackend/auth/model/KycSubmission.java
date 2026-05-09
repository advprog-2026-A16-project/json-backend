package id.ac.ui.cs.advprog.jsonbackend.auth.model;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.KycStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "kyc_submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KycSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String kycFullName;
    private String identityNumber;
    private String socialMediaLink;

    @Enumerated(EnumType.STRING)
    private KycStatus status;

    private LocalDateTime submittedAt;
    private LocalDateTime processedAt;
}