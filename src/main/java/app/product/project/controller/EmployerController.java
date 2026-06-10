package app.product.project.controller;

import app.product.project.model.dto.request.InterviewRequestDTO;
import app.product.project.model.dto.request.JobPostingRequestDTO;
import app.product.project.model.dto.request.UpdateApplicationStatusRequest;
import app.product.project.model.dto.response.ApiDataResponse;
import app.product.project.model.dto.response.ApplicationResponseDTO;
import app.product.project.model.dto.response.ApplicationStatusHistoryResponseDTO;
import app.product.project.model.dto.response.InterviewResponseDTO;
import app.product.project.model.dto.response.JobPostingResponseDTO;
import app.product.project.model.dto.response.PaginatedResponse;
import app.product.project.security.principal.CustomUserDetails;
import app.product.project.service.EmployerApplicationService;
import app.product.project.service.EmployerJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employers")
@RequiredArgsConstructor
@Slf4j
public class EmployerController {
    private final EmployerJobService employerJobService;
    private final EmployerApplicationService employerApplicationService;

    private Long getEmployerIdFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUserId();
        }
        log.error("Could not extract employer ID from authentication");
        throw new RuntimeException("Không thể lấy thông tin người dùng");
    }

    @PostMapping("/jobs")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiDataResponse<JobPostingResponseDTO>> createJobPosting(
            @Valid @RequestBody JobPostingRequestDTO requestDTO) {
        Long employerId = getEmployerIdFromToken();
        log.info("Employer {} creating job posting: {}", employerId, requestDTO.getTitle());
        JobPostingResponseDTO jobDTO = employerJobService.createJobPosting(employerId, requestDTO);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Tạo tin tuyển dụng thành công",
                jobDTO,
                null,
                HttpStatus.CREATED
        ), HttpStatus.CREATED);
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiDataResponse<PaginatedResponse<JobPostingResponseDTO>>> getMyJobPostings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Long employerId = getEmployerIdFromToken();
        log.info("Employer {} fetching job postings - Page: {}, Size: {}", employerId, page, size);
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PaginatedResponse<JobPostingResponseDTO> jobs = employerJobService.getEmployerJobPostings(employerId, pageable);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy danh sách tin tuyển dụng thành công",
                jobs,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @GetMapping("/jobs/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiDataResponse<JobPostingResponseDTO>> getJobPostingById(@PathVariable Long jobId) {
        Long employerId = getEmployerIdFromToken();
        log.info("Employer {} fetching job posting by ID: {}", employerId, jobId);
        JobPostingResponseDTO jobDTO = employerJobService.getJobPostingById(employerId, jobId);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy thông tin tin tuyển dụng thành công",
                jobDTO,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PutMapping("/jobs/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiDataResponse<JobPostingResponseDTO>> updateJobPosting(
            @PathVariable Long jobId,
            @Valid @RequestBody JobPostingRequestDTO requestDTO) {
        Long employerId = getEmployerIdFromToken();
        log.info("Employer {} updating job posting: {}", employerId, jobId);
        JobPostingResponseDTO jobDTO = employerJobService.updateJobPosting(employerId, jobId, requestDTO);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Cập nhật tin tuyển dụng thành công",
                jobDTO,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @DeleteMapping("/jobs/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiDataResponse<Object>> deleteJobPosting(@PathVariable Long jobId) {
        Long employerId = getEmployerIdFromToken();
        log.info("Employer {} deleting job posting: {}", employerId, jobId);
        employerJobService.deleteJobPosting(employerId, jobId);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Xóa tin tuyển dụng thành công",
                null,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    // Application Management Endpoints

    @GetMapping("/applications/jobs/{jobId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiDataResponse<PaginatedResponse<ApplicationResponseDTO>>> getApplicationsByJobPosting(
            @PathVariable Long jobId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Long employerId = getEmployerIdFromToken();
        log.info("Employer {} fetching applications for job {} - Page: {}, Size: {}", employerId, jobId, page, size);
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PaginatedResponse<ApplicationResponseDTO> applications = employerApplicationService.getApplicationsByJobPosting(employerId, jobId, pageable);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy danh sách hồ sơ ứng tuyển thành công",
                applications,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @GetMapping("/applications/{applicationId}")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiDataResponse<ApplicationResponseDTO>> getApplicationById(@PathVariable Long applicationId) {
        Long employerId = getEmployerIdFromToken();
        log.info("Employer {} fetching application {}", employerId, applicationId);
        ApplicationResponseDTO application = employerApplicationService.getApplicationById(employerId, applicationId);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy thông tin hồ sơ ứng tuyển thành công",
                application,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PutMapping("/applications/{applicationId}/status")
    @PreAuthorize("hasRole('EMPLOYER')")
    public ResponseEntity<ApiDataResponse<ApplicationResponseDTO>> updateApplicationStatus(
            @PathVariable Long applicationId,
            @Valid @RequestBody UpdateApplicationStatusRequest requestDTO) {
        Long employerId = getEmployerIdFromToken();
        log.info("Employer {} updating application {} status to {}", employerId, applicationId, requestDTO.getStatus());

        // Ensure the applicationId in path matches the request body
        requestDTO.setApplicationId(applicationId);
        ApplicationResponseDTO application = employerApplicationService.updateApplicationStatus(employerId, requestDTO);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Cập nhật trạng thái hồ sơ ứng tuyển thành công",
                application,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }
}

