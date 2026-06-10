package app.product.project.service.impl;

import app.product.project.model.dto.request.JobPostingRequestDTO;
import app.product.project.model.dto.response.JobPostingResponseDTO;
import app.product.project.model.dto.response.PaginatedResponse;
import app.product.project.model.entity.JobPosting;
import app.product.project.model.entity.JobStatus;
import app.product.project.model.entity.Users;
import app.product.project.repository.JobPostingRepository;
import app.product.project.repository.JobStatusRepository;
import app.product.project.repository.UserRepository;
import app.product.project.service.AdminJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminJobServiceImpl implements AdminJobService {
    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final JobStatusRepository jobStatusRepository;

    @Override
    public JobPostingResponseDTO createJobPosting(JobPostingRequestDTO requestDTO) {
        log.info("Creating new job posting: {}", requestDTO.getTitle());

        Users employer = userRepository.findById(requestDTO.getEmployerId())
                .orElseThrow(() -> {
                    log.error("Employer not found: {}", requestDTO.getEmployerId());
                    return new RuntimeException("Nhà tuyển dụng không tồn tại");
                });

        JobStatus status = jobStatusRepository.findById(requestDTO.getStatusId())
                .orElseThrow(() -> {
                    log.error("Job status not found: {}", requestDTO.getStatusId());
                    return new RuntimeException("Trạng thái job không tồn tại");
                });

        JobPosting jobPosting = JobPosting.builder()
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .salaryRange(requestDTO.getSalaryRange())
                .employer(employer)
                .status(status)
                .build();

        JobPosting savedJobPosting = jobPostingRepository.save(jobPosting);
        log.info("Job posting created successfully: {}", savedJobPosting.getId());
        return toJobPostingResponseDTO(savedJobPosting);
    }

    @Override
    public JobPostingResponseDTO updateJobPosting(Long jobId, JobPostingRequestDTO requestDTO) {
        log.info("Updating job posting: {}", jobId);

        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> {
                    log.error("Job posting not found: {}", jobId);
                    return new RuntimeException("Tin tuyển dụng không tồn tại");
                });

        Users employer = userRepository.findById(requestDTO.getEmployerId())
                .orElseThrow(() -> {
                    log.error("Employer not found: {}", requestDTO.getEmployerId());
                    return new RuntimeException("Nhà tuyển dụng không tồn tại");
                });

        JobStatus status = jobStatusRepository.findById(requestDTO.getStatusId())
                .orElseThrow(() -> {
                    log.error("Job status not found: {}", requestDTO.getStatusId());
                    return new RuntimeException("Trạng thái job không tồn tại");
                });

        jobPosting.setTitle(requestDTO.getTitle());
        jobPosting.setDescription(requestDTO.getDescription());
        jobPosting.setSalaryRange(requestDTO.getSalaryRange());
        jobPosting.setEmployer(employer);
        jobPosting.setStatus(status);

        JobPosting updatedJobPosting = jobPostingRepository.save(jobPosting);
        log.info("Job posting updated successfully: {}", jobId);
        return toJobPostingResponseDTO(updatedJobPosting);
    }

    @Override
    public PaginatedResponse<JobPostingResponseDTO> getAllJobPostings(Pageable pageable) {
        log.info("Fetching all job postings with pagination - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<JobPosting> jobPostingsPage = jobPostingRepository.findAll(pageable);

        // Using Stream API to map entities to DTOs
        List<JobPostingResponseDTO> jobPostingDTOs = jobPostingsPage.getContent()
                .stream()
                .map(this::toJobPostingResponseDTO)
                .collect(Collectors.toList());

        log.info("Retrieved {} job postings", jobPostingDTOs.size());
        return PaginatedResponse.<JobPostingResponseDTO>builder()
                .content(jobPostingDTOs)
                .pageNumber(jobPostingsPage.getNumber())
                .pageSize(jobPostingsPage.getSize())
                .totalElements(jobPostingsPage.getTotalElements())
                .totalPages(jobPostingsPage.getTotalPages())
                .isLast(jobPostingsPage.isLast())
                .build();
    }

    @Override
    public JobPostingResponseDTO getJobPostingById(Long jobId) {
        log.info("Fetching job posting: {}", jobId);

        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> {
                    log.error("Job posting not found: {}", jobId);
                    return new RuntimeException("Tin tuyển dụng không tồn tại");
                });

        return toJobPostingResponseDTO(jobPosting);
    }

    @Override
    public void updateJobStatus(Long jobId, Long statusId) {
        log.info("Updating job status for job: {}", jobId);

        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> {
                    log.error("Job posting not found: {}", jobId);
                    return new RuntimeException("Tin tuyển dụng không tồn tại");
                });

        JobStatus status = jobStatusRepository.findById(statusId)
                .orElseThrow(() -> {
                    log.error("Job status not found: {}", statusId);
                    return new RuntimeException("Trạng thái job không tồn tại");
                });

        jobPosting.setStatus(status);
        jobPostingRepository.save(jobPosting);
        log.info("Job status updated successfully for job: {}", jobId);
    }

    @Override
    public void deleteJobPosting(Long jobId) {
        log.info("Deleting job posting: {}", jobId);

        if (!jobPostingRepository.existsById(jobId)) {
            log.error("Job posting not found: {}", jobId);
            throw new RuntimeException("Tin tuyển dụng không tồn tại");
        }

        jobPostingRepository.deleteById(jobId);
        log.info("Job posting deleted successfully: {}", jobId);
    }

    private JobPostingResponseDTO toJobPostingResponseDTO(JobPosting jobPosting) {
        return JobPostingResponseDTO.builder()
                .id(jobPosting.getId())
                .title(jobPosting.getTitle())
                .description(jobPosting.getDescription())
                .salaryRange(jobPosting.getSalaryRange())
                .employerId(jobPosting.getEmployer().getUserId())
                .employerName(jobPosting.getEmployer().getFullName())
                .statusId(jobPosting.getStatus().getId())
                .statusName(jobPosting.getStatus().getStatusName())
                .build();
    }
}

