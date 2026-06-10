package app.product.project.service.impl;

import app.product.project.exception.ConflictException;
import app.product.project.model.constants.ApplicationStatusTransitions;
import app.product.project.model.dto.request.InterviewRequestDTO;
import app.product.project.model.dto.request.UpdateApplicationStatusRequest;
import app.product.project.model.dto.response.ApplicationResponseDTO;
import app.product.project.model.dto.response.ApplicationStatusHistoryResponseDTO;
import app.product.project.model.dto.response.InterviewResponseDTO;
import app.product.project.model.dto.response.PaginatedResponse;
import app.product.project.model.entity.Application;
import app.product.project.model.entity.ApplicationStatus;
import app.product.project.model.entity.JobPosting;
import app.product.project.repository.ApplicationRepository;
import app.product.project.repository.ApplicationStatusHistoryRepository;
import app.product.project.repository.ApplicationStatusRepository;
import app.product.project.repository.InterviewRepository;
import app.product.project.repository.JobPostingRepository;
import app.product.project.service.EmployerApplicationService;
import app.product.project.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EmployerApplicationServiceImpl implements EmployerApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusRepository applicationStatusRepository;
    private final ApplicationStatusHistoryRepository statusHistoryRepository;
    private final InterviewRepository interviewRepository;
    private final JobPostingRepository jobPostingRepository;
    private final EmailService emailService;

    @Override
    public PaginatedResponse<ApplicationResponseDTO> getApplicationsByJobPosting(Long employerId, Long jobPostingId, Pageable pageable) {
        log.info("Employer {} fetching applications for job posting {} - Page: {}, Size: {}", employerId, jobPostingId, pageable.getPageNumber(), pageable.getPageSize());

        // Verify job posting exists and belongs to employer
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> {
                    log.error("Job posting not found: {}", jobPostingId);
                    return new RuntimeException("Tin tuyển dụng không tồn tại");
                });

        if (!jobPosting.getEmployer().getUserId().equals(employerId)) {
            log.error("Employer {} tried to access applications for job {} owned by {}", employerId, jobPostingId, jobPosting.getEmployer().getUserId());
            throw new RuntimeException("Bạn không có quyền truy cập hồ sơ ứng tuyển này");
        }

        Page<Application> applicationsPage = applicationRepository.findByJobPosting(jobPosting, pageable);

        List<ApplicationResponseDTO> applicationDTOs = applicationsPage.getContent()
                .stream()
                .map(this::toApplicationResponseDTO)
                .collect(Collectors.toList());

        log.info("Retrieved {} applications for job posting {}", applicationDTOs.size(), jobPostingId);
        return PaginatedResponse.<ApplicationResponseDTO>builder()
                .content(applicationDTOs)
                .pageNumber(applicationsPage.getNumber())
                .pageSize(applicationsPage.getSize())
                .totalElements(applicationsPage.getTotalElements())
                .totalPages(applicationsPage.getTotalPages())
                .isLast(applicationsPage.isLast())
                .build();
    }

    @Override
    public ApplicationResponseDTO getApplicationById(Long employerId, Long applicationId) {
        log.info("Employer {} fetching application {}", employerId, applicationId);

        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> {
                    log.error("Application not found: {}", applicationId);
                    return new RuntimeException("Hồ sơ ứng tuyển không tồn tại");
                });

        // Verify that the application belongs to a job posting owned by this employer
        if (!application.getJobPosting().getEmployer().getUserId().equals(employerId)) {
            log.error("Employer {} tried to access application {} of job {} owned by {}",
                    employerId, applicationId, application.getJobPosting().getId(),
                    application.getJobPosting().getEmployer().getUserId());
            throw new RuntimeException("Bạn không có quyền truy cập hồ sơ ứng tuyển này");
        }

        return toApplicationResponseDTO(application);
    }

    @Override
    public ApplicationResponseDTO updateApplicationStatus(Long employerId, UpdateApplicationStatusRequest requestDTO) {
        log.info("Employer {} updating application {} status to {}", employerId, requestDTO.getApplicationId(), requestDTO.getStatus());

        Application application = applicationRepository.findById(requestDTO.getApplicationId())
                .orElseThrow(() -> {
                    log.error("Application not found: {}", requestDTO.getApplicationId());
                    return new RuntimeException("Hồ sơ ứng tuyển không tồn tại");
                });

        // Verify that the application belongs to a job posting owned by this employer
        if (!application.getJobPosting().getEmployer().getUserId().equals(employerId)) {
            log.error("Employer {} tried to update application {} of job {} owned by {}",
                    employerId, requestDTO.getApplicationId(), application.getJobPosting().getId(),
                    application.getJobPosting().getEmployer().getUserId());
            throw new RuntimeException("Bạn không có quyền cập nhật hồ sơ ứng tuyển này");
        }

        // Get the new status
        ApplicationStatus newStatus = applicationStatusRepository.findByStatusName(requestDTO.getStatus())
                .orElseThrow(() -> {
                    log.error("Application status not found: {}", requestDTO.getStatus());
                    return new RuntimeException("Trạng thái ứng tuyển không tồn tại");
                });

        // Validate state transition
        String currentStatus = application.getStatus().getStatusName();
        String newStatusName = requestDTO.getStatus();

        if (!ApplicationStatusTransitions.isValidTransition(currentStatus, newStatusName)) {
            log.error("Invalid status transition from {} to {} for application {}", currentStatus, newStatusName, requestDTO.getApplicationId());
            Set<String> allowedTransitions = ApplicationStatusTransitions.getAllowedTransitions(currentStatus);
            String message = String.format("Không thể chuyển từ trạng thái '%s' sang '%s'. Các trạng thái hợp lệ: %s",
                    currentStatus, newStatusName, allowedTransitions);
            throw new ConflictException(message);
        }

        // Store old status for feedback
        String oldStatus = currentStatus;

        // Update status and feedback
        application.setStatus(newStatus);
        if (requestDTO.getFeedback() != null && !requestDTO.getFeedback().isEmpty()) {
            application.setFeedback(requestDTO.getFeedback());
        }

        Application updatedApplication = applicationRepository.save(application);
        log.info("Application {} status updated from {} to {} by employer {}",
                requestDTO.getApplicationId(), oldStatus, requestDTO.getStatus(), employerId);

        // Send email notification to candidate
        try {
            String candidateEmail = application.getCandidate().getEmail();
            String candidateName = application.getCandidate().getFullName();
            String jobTitle = application.getJobPosting().getTitle();

            String emailBody = buildStatusChangeNotificationEmail(
                    candidateName, jobTitle, oldStatus, newStatusName,
                    requestDTO.getFeedback(), application.getJobPosting().getEmployer().getFullName()
            );

            emailService.sendEmail(
                    candidateEmail,
                    "Cập nhật trạng thái hồ sơ: " + jobTitle,
                    emailBody
            );
            log.info("Notification email sent to candidate {} for application {}", candidateEmail, requestDTO.getApplicationId());
        } catch (Exception e) {
            log.error("Failed to send notification email for application {}", requestDTO.getApplicationId(), e);
            // Don't throw exception, just log it
        }

        return toApplicationResponseDTO(updatedApplication);
    }

    @Override
    public InterviewResponseDTO scheduleInterview(Long employerId, Long applicationId, InterviewRequestDTO requestDTO) {
        return null;
    }

    @Override
    public PaginatedResponse<InterviewResponseDTO> getInterviewsByApplication(Long employerId, Long applicationId, Pageable pageable) {
        return null;
    }

    @Override
    public InterviewResponseDTO updateInterviewResult(Long employerId, Long interviewId, String result, String feedback) {
        return null;
    }

    @Override
    public PaginatedResponse<ApplicationStatusHistoryResponseDTO> getApplicationStatusHistory(Long employerId, Long applicationId, Pageable pageable) {
        return null;
    }

    private String buildStatusChangeNotificationEmail(String candidateName, String jobTitle, String oldStatus,
                                                      String newStatus, String feedback, String employerName) {
        String statusMessage = getStatusMessageInVietnamese(newStatus);

        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Xin chào ").append(candidateName).append(",\n\n");
        emailBody.append("Trạng thái hồ sơ ứng tuyển của bạn cho vị trí: ").append(jobTitle).append(" đã được cập nhật.\n\n");
        emailBody.append("Trạng thái cũ: ").append(oldStatus).append("\n");
        emailBody.append("Trạng thái mới: ").append(statusMessage).append("\n\n");

        if (feedback != null && !feedback.isEmpty()) {
            emailBody.append("Phản hồi từ ").append(employerName).append(":\n");
            emailBody.append(feedback).append("\n\n");
        }

        if ("ACCEPTED".equals(newStatus)) {
            emailBody.append("Chúc mừng! Bạn đã được chọn và sẽ nhận được chi tiết về offer.\n\n");
        } else if ("REJECTED".equals(newStatus)) {
            emailBody.append("Cảm ơn bạn đã ứng tuyển. Bạn có thể tiếp tục ứng tuyển các vị trí khác.\n\n");
        } else {
            emailBody.append("Quá trình ứng tuyển của bạn đang diễn ra. Vui lòng chờ thêm thông tin từ nhà tuyển dụng.\n\n");
        }

        emailBody.append("Trân trọng,\n");
        emailBody.append("Hệ thống tuyển dụng");

        return emailBody.toString();
    }

    private String getStatusMessageInVietnamese(String status) {
        return switch (status) {
            case "PENDING" -> "Chờ xét duyệt (Ứng viên nộp hồ sơ)";
            case "REVIEWING" -> "Đang xem xét (Nhà tuyển dụng mở xem CV)";
            case "INTERVIEWING" -> "Yêu cầu phỏng vấn (Hẹn phỏng vấn)";
            case "ACCEPTED" -> "Chấp nhận (Trúng tuyển - Gửi Offer)";
            case "REJECTED" -> "Từ chối (Không phù hợp/ Trượt phỏng vấn)";
            default -> status;
        };
    }

    private ApplicationResponseDTO toApplicationResponseDTO(Application application) {
        return ApplicationResponseDTO.builder()
                .id(application.getId())
                .candidateId(application.getCandidate().getUserId())
                .candidateName(application.getCandidate().getFullName())
                .jobId(application.getJobPosting().getId())
                .jobTitle(application.getJobPosting().getTitle())
                .coverLetter(application.getCoverLetter())
                .cvUrl(application.getCvUrl())
                .status(application.getStatus().getStatusName())
                .feedback(application.getFeedback())
                .appliedAt(application.getAppliedAt())
                .build();
    }
}

