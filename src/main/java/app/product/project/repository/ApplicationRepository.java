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
    boolean existsByJobPostingAndCandidate(JobPosting jobPosting, Users candidate);

    List<Application> findByJobPosting(JobPosting jobPosting);

    Page<Application> findByJobPosting(JobPosting jobPosting, Pageable pageable);

    List<Application> findByCandidate(Users candidate);

    Optional<Application> findByJobPostingAndCandidate(JobPosting jobPosting, Users candidate);

    @Query("SELECT a FROM Application a WHERE a.jobPosting.employer.userId = :employerId AND a.jobPosting.id = :jobPostingId")
    Page<Application> findByEmployerAndJobPosting(@Param("employerId") Long employerId, @Param("jobPostingId") Long jobPostingId, Pageable pageable);
}

