package app.product.project.controller;

import app.product.project.model.dto.response.ApiDataResponse;
import app.product.project.model.dto.response.JobPostingResponseDTO;
import app.product.project.model.dto.response.PaginatedResponse;
import app.product.project.service.JobSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobSearchController {
    private final JobSearchService jobSearchService;

    @GetMapping
    public ResponseEntity<ApiDataResponse<PaginatedResponse<JobPostingResponseDTO>>> searchJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String salaryRange,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Public job search - Page: {}, Size: {}, Keyword: {}, Salary: {}", page, size, keyword, salaryRange);

        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ?
            Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PaginatedResponse<JobPostingResponseDTO> jobs = jobSearchService.searchJobs(keyword, salaryRange, pageable);

        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Tìm kiếm việc làm thành công",
                jobs,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<ApiDataResponse<JobPostingResponseDTO>> getJobDetails(@PathVariable Long jobId) {
        log.info("Public job details request - Job ID: {}", jobId);

        JobPostingResponseDTO job = jobSearchService.getJobPostingById(jobId);

        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy thông tin việc làm thành công",
                job,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }
}

