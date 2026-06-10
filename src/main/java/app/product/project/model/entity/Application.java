package app.product.project.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "application")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "cv_url")
    private String cvUrl;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @ManyToOne
    @JoinColumn(name = "application_status_id")
    private ApplicationStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users candidate;

    @ManyToOne
    @JoinColumn(name = "job_posting_id")
    @JsonIgnore
    private JobPosting jobPosting;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

}

