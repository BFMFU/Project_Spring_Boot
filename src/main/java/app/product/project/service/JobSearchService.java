package app.product.project.service;

import app.product.project.model.dto.response.JobPostingResponseDTO;
import app.product.project.model.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

public interface JobSearchService {
    PaginatedResponse<JobPostingResponseDTO> searchJobs(String keyword, String salaryRange, Pageable pageable);

    JobPostingResponseDTO getJobPostingById(Long jobId);
}

