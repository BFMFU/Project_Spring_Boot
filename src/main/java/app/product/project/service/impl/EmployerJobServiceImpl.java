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
import app.product.project.service.EmployerJobService;
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
public class EmployerJobServiceImpl implements EmployerJobService {
    private final JobPostingRepository jobPostingRepository;
    private final UserRepository userRepository;
    private final JobStatusRepository jobStatusRepository;

    @Override
    public JobPostingResponseDTO createJobPosting(Long employerId, JobPostingRequestDTO requestDTO) {
        log.info("Employer {} creating new job posting: {}", employerId, requestDTO.getTitle());

        Users employer = userRepository.findById(employerId)
                .orElseThrow(() -> {
                    log.error("Employer not found: {}", employerId);
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
        log.info("Job posting created successfully by employer {}: {}", employerId, savedJobPosting.getId());
        return toJobPostingResponseDTO(savedJobPosting);
    }

    @Override
    public JobPostingResponseDTO updateJobPosting(Long employerId, Long jobId, JobPostingRequestDTO requestDTO) {
        log.info("Employer {} updating job posting: {}", employerId, jobId);

        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> {
                    log.error("Job posting not found: {}", jobId);
                    return new RuntimeException("Tin tuyển dụng không tồn tại");
                });

        // Verify that the job belongs to this employer
        if (!jobPosting.getEmployer().getUserId().equals(employerId)) {
            log.error("Employer {} tried to update job {} owned by {}", employerId, jobId, jobPosting.getEmployer().getUserId());
            throw new RuntimeException("Bạn không có quyền chỉnh sửa tin tuyển dụng này");
        }

        JobStatus status = jobStatusRepository.findById(requestDTO.getStatusId())
                .orElseThrow(() -> {
                    log.error("Job status not found: {}", requestDTO.getStatusId());
                    return new RuntimeException("Trạng thái job không tồn tại");
                });

        jobPosting.setTitle(requestDTO.getTitle());
        jobPosting.setDescription(requestDTO.getDescription());
        jobPosting.setSalaryRange(requestDTO.getSalaryRange());
        jobPosting.setStatus(status);

        JobPosting updatedJobPosting = jobPostingRepository.save(jobPosting);
        log.info("Job posting updated successfully by employer {}: {}", employerId, jobId);
        return toJobPostingResponseDTO(updatedJobPosting);
    }

    @Override
    public PaginatedResponse<JobPostingResponseDTO> getEmployerJobPostings(Long employerId, Pageable pageable) {
        log.info("Fetching all job postings for employer {} with pagination - Page: {}, Size: {}", employerId, pageable.getPageNumber(), pageable.getPageSize());

        // Verify employer exists
        if (!userRepository.existsById(employerId)) {
            log.error("Employer not found: {}", employerId);
            throw new RuntimeException("Nhà tuyển dụng không tồn tại");
        }

        Page<JobPosting> jobPostingsPage = jobPostingRepository.findByEmployerUserId(employerId, pageable);

        // Using Stream API to map entities to DTOs
        List<JobPostingResponseDTO> jobPostingDTOs = jobPostingsPage.getContent()
                .stream()
                .map(this::toJobPostingResponseDTO)
                .collect(Collectors.toList());

        log.info("Retrieved {} job postings for employer {}", jobPostingDTOs.size(), employerId);
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
    public JobPostingResponseDTO getJobPostingById(Long employerId, Long jobId) {
        log.info("Fetching job posting {} for employer {}", jobId, employerId);

        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> {
                    log.error("Job posting not found: {}", jobId);
                    return new RuntimeException("Tin tuyển dụng không tồn tại");
                });

        // Verify that the job belongs to this employer
        if (!jobPosting.getEmployer().getUserId().equals(employerId)) {
            log.error("Employer {} tried to access job {} owned by {}", employerId, jobId, jobPosting.getEmployer().getUserId());
            throw new RuntimeException("Bạn không có quyền truy cập tin tuyển dụng này");
        }

        return toJobPostingResponseDTO(jobPosting);
    }

    @Override
    public void deleteJobPosting(Long employerId, Long jobId) {
        log.info("Employer {} deleting job posting: {}", employerId, jobId);

        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> {
                    log.error("Job posting not found: {}", jobId);
                    return new RuntimeException("Tin tuyển dụng không tồn tại");
                });

        // Verify that the job belongs to this employer
        if (!jobPosting.getEmployer().getUserId().equals(employerId)) {
            log.error("Employer {} tried to delete job {} owned by {}", employerId, jobId, jobPosting.getEmployer().getUserId());
            throw new RuntimeException("Bạn không có quyền xóa tin tuyển dụng này");
        }

        jobPostingRepository.deleteById(jobId);
        log.info("Job posting deleted successfully by employer {}: {}", employerId, jobId);
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

