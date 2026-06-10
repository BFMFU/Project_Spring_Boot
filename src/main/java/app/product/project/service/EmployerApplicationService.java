package app.product.project.service;

import app.product.project.model.dto.request.InterviewRequestDTO;
import app.product.project.model.dto.request.UpdateApplicationStatusRequest;
import app.product.project.model.dto.response.ApplicationResponseDTO;
import app.product.project.model.dto.response.ApplicationStatusHistoryResponseDTO;
import app.product.project.model.dto.response.InterviewResponseDTO;
import app.product.project.model.dto.response.PaginatedResponse;
import org.springframework.data.domain.Pageable;

public interface EmployerApplicationService {

    PaginatedResponse<ApplicationResponseDTO> getApplicationsByJobPosting(Long employerId, Long jobPostingId, Pageable pageable);

    ApplicationResponseDTO getApplicationById(Long employerId, Long applicationId);

    ApplicationResponseDTO updateApplicationStatus(Long employerId, UpdateApplicationStatusRequest requestDTO);

    // Interview Management
    InterviewResponseDTO scheduleInterview(Long employerId, Long applicationId, InterviewRequestDTO requestDTO);

    PaginatedResponse<InterviewResponseDTO> getInterviewsByApplication(Long employerId, Long applicationId, Pageable pageable);

    InterviewResponseDTO updateInterviewResult(Long employerId, Long interviewId, String result, String feedback);

    // Status History
    PaginatedResponse<ApplicationStatusHistoryResponseDTO> getApplicationStatusHistory(Long employerId, Long applicationId, Pageable pageable);
}

