package app.product.project.controller;

import app.product.project.model.dto.request.ApplicationRequestDTO;
import app.product.project.model.dto.response.ApiDataResponse;
import app.product.project.model.dto.response.ApplicationResponseDTO;
import app.product.project.model.dto.response.UserResponseDTO;
import app.product.project.security.principal.CustomUserDetails;
import app.product.project.service.CandidateApplicationService;
import app.product.project.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
@Slf4j
public class CandidateController {
    private final CandidateApplicationService candidateApplicationService;
    private final UserService userService;

    @PostMapping("/applications")
    public ResponseEntity<ApiDataResponse<ApplicationResponseDTO>> submitApplication(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ApplicationRequestDTO requestDTO) {
        try {
            log.info("Candidate {} submitting job application for job ID: {}",
                    userDetails.getUserId(), requestDTO.getJobId());

            ApplicationResponseDTO response = candidateApplicationService.submitApplication(
                    userDetails.getUserId(),
                    requestDTO
            );

            return new ResponseEntity<>(new ApiDataResponse<>(
                    true,
                    "Nộp hồ sơ thành công",
                    response,
                    null,
                    HttpStatus.CREATED
            ), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error submitting application for candidate {}: {}",
                    userDetails.getUserId(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/cv/upload")
    public ResponseEntity<ApiDataResponse<UserResponseDTO>> uploadCV(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("file") MultipartFile cvFile) {
        try {
            log.info("Candidate {} uploading CV file: {}", userDetails.getUserId(), cvFile.getOriginalFilename());

            UserResponseDTO response = userService.uploadCVFile(userDetails.getUserId(), cvFile);

            return new ResponseEntity<>(new ApiDataResponse<>(
                    true,
                    "Tải lên CV thành công",
                    response,
                    null,
                    HttpStatus.OK
            ), HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error uploading CV for candidate {}: {}",
                    userDetails.getUserId(), e.getMessage(), e);
            throw e;
        }
    }
}

