package app.product.project.repository;

import app.product.project.model.entity.Application;
import app.product.project.model.entity.Interview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {
    /**
     * Get interviews for a specific application
     */
    List<Interview> findByApplication(Application application);

    /**
     * Get interviews for a specific application with pagination
     */
    Page<Interview> findByApplication(Application application, Pageable pageable);

    /**
     * Get total count of interviews for an application
     */
    long countByApplication(Application application);

    /**
     * Find upcoming interviews
     */
    List<Interview> findByScheduledDateAfter(LocalDateTime date);

    /**
     * Find interviews by result status
     */
    List<Interview> findByApplicationAndResult(Application application, String result);
}

