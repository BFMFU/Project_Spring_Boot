package app.product.project.service.impl;

import app.product.project.exception.ConflictException;
import app.product.project.model.dto.request.ApplicationRequestDTO;
import app.product.project.model.dto.response.ApplicationResponseDTO;
import app.product.project.model.entity.Application;
import app.product.project.model.entity.ApplicationStatus;
import app.product.project.model.entity.JobPosting;
import app.product.project.model.entity.Users;
import app.product.project.repository.ApplicationRepository;
import app.product.project.repository.ApplicationStatusRepository;
import app.product.project.repository.JobPostingRepository;
import app.product.project.repository.UserRepository;
import app.product.project.service.CandidateApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CandidateApplicationServiceImpl implements CandidateApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusRepository applicationStatusRepository;
    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;

    @Override
    public ApplicationResponseDTO submitApplication(Long candidateId, ApplicationRequestDTO requestDTO) {
        log.info("Processing application submission for candidate {} to job {}", candidateId, requestDTO.getJobId());

        // Get candidate
        Users candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> {
                    log.error("Candidate not found: {}", candidateId);
                    return new RuntimeException("Ứng viên không tồn tại");
                });

        // Get job posting
        JobPosting jobPosting = jobPostingRepository.findById(requestDTO.getJobId())
                .orElseThrow(() -> {
                    log.error("Job posting not found: {}", requestDTO.getJobId());
                    return new RuntimeException("Tin tuyển dụng không tồn tại");
                });

        // Check if job is OPEN
        if (jobPosting.getStatus() == null || !jobPosting.getStatus().getStatusName().equalsIgnoreCase("PENDING_APPROVAL")) {
            log.warn("Job posting {} is not open. Status: {}", requestDTO.getJobId(),
                    jobPosting.getStatus() != null ? jobPosting.getStatus().getStatusName() : "null");
            throw new ConflictException("Tin tuyển dụng đã đóng hoặc không khả dụng");
        }

        // Check if candidate already applied
        if (applicationRepository.existsByJobPostingAndCandidate(jobPosting, candidate)) {
            log.warn("Candidate {} already applied for job {}", candidateId, requestDTO.getJobId());
            throw new ConflictException("Bạn đã nộp hồ sơ cho tin tuyển dụng này rồi");
        }

        // Get PENDING status
        ApplicationStatus pendingStatus = applicationStatusRepository.findByStatusName("PENDING")
                .orElseThrow(() -> {
                    log.error("Application status 'PENDING' not found");
                    return new RuntimeException("Trạng thái ứng tuyển PENDING không tồn tại");
                });

        // Create application
        Application application = Application.builder()
                .candidate(candidate)
                .jobPosting(jobPosting)
                .coverLetter(requestDTO.getCoverLetter())
                .cvUrl(requestDTO.getCvUrl())
                .status(pendingStatus)
                .appliedAt(LocalDateTime.now())
                .build();

        Application savedApplication = applicationRepository.save(application);
        log.info("Application submitted successfully. Application ID: {}, Candidate ID: {}, Job ID: {}",
                savedApplication.getId(), candidateId, requestDTO.getJobId());

        return toApplicationResponseDTO(savedApplication);
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

