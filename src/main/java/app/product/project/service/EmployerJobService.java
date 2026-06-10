package app.product.project.service;

import app.product.project.model.dto.request.JobPostingRequestDTO;
import app.product.project.model.dto.response.JobPostingResponseDTO;
import app.product.project.model.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

public interface EmployerJobService {
    JobPostingResponseDTO createJobPosting(Long employerId, JobPostingRequestDTO requestDTO);

    JobPostingResponseDTO updateJobPosting(Long employerId, Long jobId, JobPostingRequestDTO requestDTO);

    PaginatedResponse<JobPostingResponseDTO> getEmployerJobPostings(Long employerId, Pageable pageable);

    JobPostingResponseDTO getJobPostingById(Long employerId, Long jobId);

    void deleteJobPosting(Long employerId, Long jobId);
}

