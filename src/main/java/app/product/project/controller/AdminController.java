package app.product.project.controller;

import app.product.project.model.dto.request.JobPostingRequestDTO;
import app.product.project.model.dto.request.UserManagementRequestDTO;
import app.product.project.model.dto.response.ApiDataResponse;
import app.product.project.model.dto.response.JobPostingResponseDTO;
import app.product.project.model.dto.response.PaginatedResponse;
import app.product.project.model.dto.response.UserResponseDTO;
import app.product.project.service.AdminJobService;
import app.product.project.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {
    private final AdminUserService adminUserService;
    private final AdminJobService adminJobService;

    @PostMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<UserResponseDTO>> createUser(
            @Valid @RequestBody UserManagementRequestDTO requestDTO) {
        log.info("Admin creating user: {}", requestDTO.getUsername());
        UserResponseDTO userDTO = adminUserService.createUser(requestDTO);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Tạo người dùng thành công",
                userDTO,
                null,
                HttpStatus.CREATED
        ), HttpStatus.CREATED);
    }


    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<PaginatedResponse<UserResponseDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection) {
        log.info("Admin fetching users - Page: {}, Size: {}", page, size);
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        PaginatedResponse<UserResponseDTO> users = adminUserService.getAllUsers(pageable);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy danh sách người dùng thành công",
                users,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiDataResponse<UserResponseDTO>> getUserById(@PathVariable Long userId) {
        log.info("Admin fetching user by ID: {}", userId);
        UserResponseDTO userDTO = adminUserService.getUserById(userId);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy thông tin người dùng thành công",
                userDTO,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PutMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<UserResponseDTO>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserManagementRequestDTO requestDTO) {
        log.info("Admin updating user: {}", userId);
        UserResponseDTO userDTO = adminUserService.updateUser(userId, requestDTO);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Cập nhật người dùng thành công",
                userDTO,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PatchMapping("/users/{userId}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<Object>> deactivateUser(@PathVariable Long userId) {
        log.info("Admin deactivating user: {}", userId);
        adminUserService.deactivateUser(userId);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Khóa người dùng thành công",
                null,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }


    @PatchMapping("/users/{userId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<Object>> activateUser(@PathVariable Long userId) {
        log.info("Admin activating user: {}", userId);
        adminUserService.activateUser(userId);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Kích hoạt người dùng thành công",
                null,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<Object>> deleteUser(@PathVariable Long userId) {
        log.info("Admin deleting user: {}", userId);
        adminUserService.deleteUser(userId);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Xóa người dùng thành công",
                null,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PostMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<JobPostingResponseDTO>> createJobPosting(
            @Valid @RequestBody JobPostingRequestDTO requestDTO) {
        log.info("Admin creating job posting: {}", requestDTO.getTitle());
        JobPostingResponseDTO jobDTO = adminJobService.createJobPosting(requestDTO);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Tạo tin tuyển dụng thành công",
                jobDTO,
                null,
                HttpStatus.CREATED
        ), HttpStatus.CREATED);
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<PaginatedResponse<JobPostingResponseDTO>>> getAllJobPostings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        log.info("Admin fetching job postings - Page: {}, Size: {}", page, size);
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        PaginatedResponse<JobPostingResponseDTO> jobs = adminJobService.getAllJobPostings(pageable);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy danh sách tin tuyển dụng thành công",
                jobs,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @GetMapping("/jobs/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<JobPostingResponseDTO>> getJobPostingById(@PathVariable Long jobId) {
        log.info("Admin fetching job posting by ID: {}", jobId);
        JobPostingResponseDTO jobDTO = adminJobService.getJobPostingById(jobId);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy thông tin tin tuyển dụng thành công",
                jobDTO,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PutMapping("/jobs/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<JobPostingResponseDTO>> updateJobPosting(
            @PathVariable Long jobId,
            @Valid @RequestBody JobPostingRequestDTO requestDTO) {
        log.info("Admin updating job posting: {}", jobId);
        JobPostingResponseDTO jobDTO = adminJobService.updateJobPosting(jobId, requestDTO);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Cập nhật tin tuyển dụng thành công",
                jobDTO,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PatchMapping("/jobs/{jobId}/status/{statusId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<Object>> updateJobStatus(
            @PathVariable Long jobId,
            @PathVariable Long statusId) {
        log.info("Admin updating job status for job: {}", jobId);
        adminJobService.updateJobStatus(jobId, statusId);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Cập nhật trạng thái tin tuyển dụng thành công",
                null,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @DeleteMapping("/jobs/{jobId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiDataResponse<Object>> deleteJobPosting(@PathVariable Long jobId) {
        log.info("Admin deleting job posting: {}", jobId);
        adminJobService.deleteJobPosting(jobId);
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Xóa tin tuyển dụng thành công",
                null,
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }
}
