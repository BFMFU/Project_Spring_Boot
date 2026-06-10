package app.product.project.repository;

import app.product.project.model.entity.Application;
import app.product.project.model.entity.JobPosting;
import app.product.project.model.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    /**
     * Check if a candidate has already applied for a job posting
     */
    boolean existsByJobPostingAndCandidate(JobPosting jobPosting, Users candidate);

    /**
     * Get all applications for a specific job posting
     */
    List<Application> findByJobPosting(JobPosting jobPosting);

    /**
     * Get applications for a specific job posting with pagination
     */
    Page<Application> findByJobPosting(JobPosting jobPosting, Pageable pageable);

    /**
     * Get all applications from a specific candidate
     */
    List<Application> findByCandidate(Users candidate);

    /**
     * Get a specific application
     */
    Optional<Application> findByJobPostingAndCandidate(JobPosting jobPosting, Users candidate);

    /**
     * Find applications by employer and job posting
     */
    @Query("SELECT a FROM Application a WHERE a.jobPosting.employer.userId = :employerId AND a.jobPosting.id = :jobPostingId")
    Page<Application> findByEmployerAndJobPosting(@Param("employerId") Long employerId, @Param("jobPostingId") Long jobPostingId, Pageable pageable);
}

