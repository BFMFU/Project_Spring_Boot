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
    List<Interview> findByApplication(Application application);

    Page<Interview> findByApplication(Application application, Pageable pageable);

    long countByApplication(Application application);

    List<Interview> findByScheduledDateAfter(LocalDateTime date);

    List<Interview> findByApplicationAndResult(Application application, String result);
}

