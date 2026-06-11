package app.product.project.service.impl;

import app.product.project.exception.ConflictException;
import app.product.project.model.constants.ApplicationStatusTransitions;
import app.product.project.model.dto.request.InterviewRequestDTO;
import app.product.project.model.dto.request.UpdateApplicationStatusRequest;
import app.product.project.model.dto.response.ApplicationResponseDTO;
import app.product.project.model.dto.response.InterviewResponseDTO;
import app.product.project.model.dto.response.PaginatedResponse;
import app.product.project.model.entity.*;
import app.product.project.repository.*;
import app.product.project.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmployerApplicationServiceImplTests {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationStatusRepository applicationStatusRepository;

    @Mock
    private ApplicationStatusHistoryRepository statusHistoryRepository;

    @Mock
    private InterviewRepository interviewRepository;

    @Mock
    private JobPostingRepository jobPostingRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmployerApplicationServiceImpl employerApplicationService;

    private Application application;
    private ApplicationStatus applicationStatus;
    private JobPosting jobPosting;
    private JobStatus jobStatus;
    private Users employer;
    private Users candidate;
    private Interview interview;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup employer user
        employer = Users.builder()
                .userId(1L)
                .username("employer@example.com")
                .fullName("Employer Name")
                .email("employer@example.com")
                .build();

        // Setup candidate user
        candidate = Users.builder()
                .userId(2L)
                .username("candidate@example.com")
                .fullName("Candidate Name")
                .email("candidate@example.com")
                .build();

        // Setup job status
        jobStatus = JobStatus.builder()
                .id(1L)
                .statusName("ACTIVE")
                .build();

        // Setup job posting
        jobPosting = JobPosting.builder()
                .id(1L)
                .title("Senior Java Developer")
                .description("We are looking for a senior Java developer")
                .salaryRange("10000000 - 22000000 VND")
                .employer(employer)
                .status(jobStatus)
                .applications(null)
                .build();

        // Setup application status
        applicationStatus = ApplicationStatus.builder()
                .id(1L)
                .statusName("PENDING")
                .build();

        // Setup application
        application = Application.builder()
                .id(1L)
                .candidate(candidate)
                .jobPosting(jobPosting)
                .status(applicationStatus)
                .coverLetter("I'm interested")
                .cvUrl("https://example.com/cv.pdf")
                .appliedAt(LocalDateTime.now())
                .build();

        // Setup interview
        interview = Interview.builder()
                .id(1L)
                .application(application)
                .scheduledDate(LocalDateTime.now().plusDays(7))
                .interviewType("Online")
                .location("Google Meet")
                .notes("Technical interview")
                .result("PENDING")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetApplicationsByJobPosting_Success() {
        Long employerId = 1L;
        Long jobId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        List<Application> applicationList = new ArrayList<>();
        applicationList.add(application);
        Page<Application> applicationPage = new PageImpl<>(applicationList, pageable, 1);

        when(jobPostingRepository.findById(jobId)).thenReturn(Optional.of(jobPosting));
        when(applicationRepository.findByJobPosting(jobPosting, pageable)).thenReturn(applicationPage);

        PaginatedResponse<ApplicationResponseDTO> result = employerApplicationService
                .getApplicationsByJobPosting(employerId, jobId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("PENDING", result.getContent().get(0).getStatus());
        verify(applicationRepository, times(1)).findByJobPosting(jobPosting, pageable);
    }

    @Test
    void testGetApplicationById_Success() {
        Long employerId = 1L;
        Long applicationId = 1L;

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));

        ApplicationResponseDTO result = employerApplicationService.getApplicationById(employerId, applicationId);

        assertNotNull(result);
        assertEquals("Candidate Name", result.getCandidateName());
        assertEquals("PENDING", result.getStatus());
        verify(applicationRepository, times(1)).findById(applicationId);
    }

    @Test
    void testUpdateApplicationStatus_Success() {
        Long employerId = 1L;

        UpdateApplicationStatusRequest request = UpdateApplicationStatusRequest.builder()
                .applicationId(1L)
                .status("REVIEWING")
                .feedback("CV looks good")
                .build();

        ApplicationStatus reviewingStatus = ApplicationStatus.builder()
                .id(2L)
                .statusName("REVIEWING")
                .build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationStatusRepository.findByStatusName("REVIEWING")).thenReturn(Optional.of(reviewingStatus));
        when(applicationRepository.save(any(Application.class))).thenReturn(application);
        when(statusHistoryRepository.save(any(ApplicationStatusHistory.class))).thenReturn(null);

        ApplicationResponseDTO result = employerApplicationService.updateApplicationStatus(employerId, request);

        assertNotNull(result);
        verify(applicationRepository, times(1)).save(any(Application.class));
        verify(statusHistoryRepository, times(1)).save(any(ApplicationStatusHistory.class));
        verify(emailService, times(1)).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    void testUpdateApplicationStatus_InvalidTransition() {
        Long employerId = 1L;

        UpdateApplicationStatusRequest request = UpdateApplicationStatusRequest.builder()
                .applicationId(1L)
                .status("PENDING")  // Can't transition from PENDING to PENDING
                .feedback("Test feedback")
                .build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationStatusRepository.findByStatusName("PENDING")).thenReturn(Optional.of(applicationStatus));

        assertThrows(ConflictException.class, () -> {
            employerApplicationService.updateApplicationStatus(employerId, request);
        });

        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void testScheduleInterview_Success() {
        Long employerId = 1L;
        Long applicationId = 1L;

        InterviewRequestDTO request = InterviewRequestDTO.builder()
                .scheduledDate(LocalDateTime.now().plusDays(7))
                .interviewType("Online")
                .location("Google Meet")
                .notes("Technical interview")
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(interviewRepository.save(any(Interview.class))).thenReturn(interview);

        InterviewResponseDTO result = employerApplicationService.scheduleInterview(employerId, applicationId, request);

        assertNotNull(result);
        assertEquals("Online", result.getInterviewType());
        assertEquals("Google Meet", result.getLocation());
        assertEquals("PENDING", result.getResult());
        verify(interviewRepository, times(1)).save(any(Interview.class));
    }

    @Test
    void testScheduleInterview_ApplicationNotFound() {
        Long employerId = 1L;
        Long applicationId = 999L;

        InterviewRequestDTO request = InterviewRequestDTO.builder()
                .scheduledDate(LocalDateTime.now().plusDays(7))
                .interviewType("Online")
                .location("Google Meet")
                .build();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            employerApplicationService.scheduleInterview(employerId, applicationId, request);
        });

        verify(interviewRepository, never()).save(any(Interview.class));
    }

    @Test
    void testGetApplicationsByJobPosting_NotOwner() {
        Long employerId = 999L; // different employer
        Long jobId = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        when(jobPostingRepository.findById(jobId)).thenReturn(Optional.of(jobPosting));

        // Expect RuntimeException because employerId does not match jobPosting.employer.userId
        assertThrows(RuntimeException.class, () -> {
            employerApplicationService.getApplicationsByJobPosting(employerId, jobId, pageable);
        });

        verify(applicationRepository, never()).findByJobPosting(any(JobPosting.class), any(Pageable.class));
    }

    @Test
    void testUpdateApplicationStatus_StatusNotFound() {
        Long employerId = 1L;

        UpdateApplicationStatusRequest request = UpdateApplicationStatusRequest.builder()
                .applicationId(1L)
                .status("NON_EXISTENT_STATUS")
                .feedback("Feedback")
                .build();

        when(applicationRepository.findById(1L)).thenReturn(Optional.of(application));
        when(applicationStatusRepository.findByStatusName("NON_EXISTENT_STATUS")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            employerApplicationService.updateApplicationStatus(employerId, request);
        });

        verify(applicationRepository, never()).save(any(Application.class));
    }
}

