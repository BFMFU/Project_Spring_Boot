package app.product.project.repository;

import app.product.project.model.entity.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobStatusRepository extends JpaRepository<JobStatus, Long> {
    Optional<JobStatus> findByStatusName(String statusName);
}

