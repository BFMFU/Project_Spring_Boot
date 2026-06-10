package app.product.project.repository;

import app.product.project.model.entity.Application;
import app.product.project.model.entity.ApplicationStatusHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationStatusHistoryRepository extends JpaRepository<ApplicationStatusHistory, Long> {

    List<ApplicationStatusHistory> findByApplicationOrderByChangedAtDesc(Application application);

    Page<ApplicationStatusHistory> findByApplicationOrderByChangedAtDesc(Application application, Pageable pageable);
    long countByApplication(Application application);
}

