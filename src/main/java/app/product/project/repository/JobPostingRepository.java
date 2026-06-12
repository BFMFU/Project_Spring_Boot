package app.product.project.repository;

import app.product.project.model.entity.JobPosting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    Page<JobPosting> findByEmployerUserId(Long employerId, Pageable pageable);

    @Query("SELECT j FROM JobPosting j WHERE LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<JobPosting> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);


    @Query("SELECT j FROM JobPosting j WHERE " +
            "(LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:salaryRange IS NULL OR j.salaryRange = :salaryRange)")
    Page<JobPosting> searchJobs(@Param("keyword") String keyword, @Param("salaryRange") String salaryRange, Pageable pageable);

    @Query("SELECT j FROM JobPosting j WHERE j.status.statusName = 'OPEN' ORDER BY j.id DESC")
    Page<JobPosting> findAllOpenJobs(Pageable pageable);
}
