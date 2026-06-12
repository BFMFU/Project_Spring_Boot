package app.product.project.service.impl;

import app.product.project.model.dto.response.JobPostingResponseDTO;
import app.product.project.model.dto.response.PaginatedResponse;
import app.product.project.model.entity.JobPosting;
import app.product.project.repository.JobPostingRepository;
import app.product.project.service.JobSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobSearchServiceImpl implements JobSearchService {
    private final JobPostingRepository jobPostingRepository;

    @Override
    public PaginatedResponse<JobPostingResponseDTO> searchJobs(String keyword, String salaryRange, Pageable pageable) {
        log.info("Searching jobs - Keyword: {}, Salary Range: {}, Page: {}", keyword, salaryRange, pageable.getPageNumber());

        Page<JobPosting> jobPage;

        // Search logic based on provided filters
        if (keyword != null && !keyword.trim().isEmpty() && salaryRange != null && !salaryRange.trim().isEmpty()) {
            // Search with both keyword and salary range
            jobPage = jobPostingRepository.searchJobs(keyword.trim(), salaryRange.trim(), pageable);
        } else if (keyword != null && !keyword.trim().isEmpty()) {
            // Search by keyword only
            jobPage = jobPostingRepository.searchByKeyword(keyword.trim(), pageable);
        } else if (salaryRange != null && !salaryRange.trim().isEmpty()) {
            // Search by salary range only
            jobPage = jobPostingRepository.searchJobs("", salaryRange.trim(), pageable);
        } else {
            // No filters - return all open jobs
            jobPage = jobPostingRepository.findAllOpenJobs(pageable);
        }

        // Using Stream API to map entities to DTOs
        var jobDTOs = jobPage.getContent()
                .stream()
                .map(this::toJobPostingResponseDTO)
                .collect(Collectors.toList());

        log.info("Found {} jobs matching the search criteria", jobDTOs.size());

        return PaginatedResponse.<JobPostingResponseDTO>builder()
                .content(jobDTOs)
                .pageNumber(jobPage.getNumber())
                .pageSize(jobPage.getSize())
                .totalElements(jobPage.getTotalElements())
                .totalPages(jobPage.getTotalPages())
                .isLast(jobPage.isLast())
                .build();
    }

    @Override
    public JobPostingResponseDTO getJobPostingById(Long jobId) {
        log.info("Fetching job posting details - Job ID: {}", jobId);

        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> {
                    log.error("Job posting not found: {}", jobId);
                    return new RuntimeException("Tin tuyển dụng không tồn tại");
                });

        log.info("Job posting found: {}", jobPosting.getTitle());
        return toJobPostingResponseDTO(jobPosting);
}
    private JobPostingResponseDTO toJobPostingResponseDTO(JobPosting jobPosting) {
        return JobPostingResponseDTO.builder()
                .id(jobPosting.getId())
                .title(jobPosting.getTitle())
                .description(jobPosting.getDescription())
                .salaryRange(jobPosting.getSalaryRange())
                .employerId(jobPosting.getEmployer().getUserId())
                .employerName(jobPosting.getEmployer().getFullName())
                .statusId(jobPosting.getStatus() != null ? jobPosting.getStatus().getId() : null)
                .statusName(jobPosting.getStatus() != null ? jobPosting.getStatus().getStatusName() : "UNKNOWN")
                .build();
    }
}

