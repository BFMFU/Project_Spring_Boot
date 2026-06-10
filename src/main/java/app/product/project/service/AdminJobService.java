package app.product.project.service;

import app.product.project.model.dto.request.JobPostingRequestDTO;
import app.product.project.model.dto.response.JobPostingResponseDTO;
import app.product.project.model.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

public interface AdminJobService {
    JobPostingResponseDTO createJobPosting(JobPostingRequestDTO requestDTO);

    JobPostingResponseDTO updateJobPosting(Long jobId, JobPostingRequestDTO requestDTO);

    PaginatedResponse<JobPostingResponseDTO> getAllJobPostings(Pageable pageable);

    JobPostingResponseDTO getJobPostingById(Long jobId);

    void updateJobStatus(Long jobId, Long statusId);

    void deleteJobPosting(Long jobId);
}

