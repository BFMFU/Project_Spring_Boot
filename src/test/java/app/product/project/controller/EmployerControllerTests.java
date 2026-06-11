package app.product.project.controller;

import app.product.project.model.dto.request.JobPostingRequestDTO;
import app.product.project.model.dto.request.UpdateApplicationStatusRequest;
import app.product.project.model.dto.response.ApplicationResponseDTO;
import app.product.project.model.dto.response.JobPostingResponseDTO;
import app.product.project.model.dto.response.PaginatedResponse;
import app.product.project.security.principal.CustomUserDetails;
import app.product.project.service.EmployerApplicationService;
import app.product.project.service.EmployerJobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;
    @MockitoBean
    private EmployerJobService employerJobService;

    @MockitoBean
    private EmployerApplicationService employerApplicationService;

    private JobPostingRequestDTO jobPostingRequestDTO;
    private JobPostingResponseDTO jobPostingResponseDTO;
    private ApplicationResponseDTO applicationResponseDTO;
    private UpdateApplicationStatusRequest updateStatusRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        // Setup JobPostingRequestDTO
        jobPostingRequestDTO = JobPostingRequestDTO.builder()
                .title("Senior Java Developer")
                .description("We are looking for a senior Java developer")
                .salaryRange("10000000 - 22000000 VND")
                .build();

        // Setup JobPostingResponseDTO
        jobPostingResponseDTO = JobPostingResponseDTO.builder()
                .id(1L)
                .title("Senior Java Developer")
                .description("We are looking for a senior Java developer")
                .salaryRange("10000000 - 22000000 VND")
                .employerId(1L)
                .employerName("Tech Company")
                .build();

        // Setup ApplicationResponseDTO
        applicationResponseDTO = ApplicationResponseDTO.builder()
                .id(1L)
                .candidateId(2L)
                .candidateName("John Doe")
                .jobId(1L)
                .jobTitle("Senior Java Developer")
                .coverLetter("I'm interested in this position")
                .cvUrl("https://example.com/cv.pdf")
                .status("PENDING")
                .appliedAt(LocalDateTime.now())
                .build();

        // Setup UpdateApplicationStatusRequest
        updateStatusRequest = UpdateApplicationStatusRequest.builder()
                .applicationId(1L)
                .status("REVIEWING")
                .feedback("CV looks good")
                .build();
    }

    @Test
    void testCreateJobPosting_Success() throws Exception {
        when(employerJobService.createJobPosting(anyLong(), any()))
                .thenReturn(jobPostingResponseDTO);

        mockMvc.perform(post("/api/v1/employers/jobs")
                                .with(employerAuth())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(jobPostingRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)));
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testGetMyJobPostings_Success() throws Exception {
        List<JobPostingResponseDTO> jobList = new ArrayList<>();
        jobList.add(jobPostingResponseDTO);

        PaginatedResponse<JobPostingResponseDTO> paginatedResponse = PaginatedResponse.<JobPostingResponseDTO>builder()
                .content(jobList)
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        when(employerJobService.getEmployerJobPostings(anyLong(), any()))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/v1/employers/jobs")
                                .with(employerAuth())
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(employerJobService, times(1)).getEmployerJobPostings(anyLong(), any());
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testGetJobPostingById_Success() throws Exception {
        when(employerJobService.getJobPostingById(anyLong(), anyLong()))
                .thenReturn(jobPostingResponseDTO);

        mockMvc.perform(get("/api/v1/employers/jobs/1")
                                .with(employerAuth())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(employerJobService, times(1)).getJobPostingById(anyLong(), anyLong());
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testUpdateApplicationStatus_Success() throws Exception {
        ApplicationResponseDTO updatedApplication = ApplicationResponseDTO.builder()
                .id(1L)
                .candidateId(2L)
                .candidateName("John Doe")
                .jobId(1L)
                .jobTitle("Senior Java Developer")
                .status("REVIEWING")
                .feedback("CV looks good")
                .appliedAt(LocalDateTime.now())
                .build();

        when(employerApplicationService.updateApplicationStatus(anyLong(), any()))
                .thenReturn(updatedApplication);

        mockMvc.perform(put("/api/v1/employers/applications/1/status")
                                .with(employerAuth())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateStatusRequest)))
                .andExpect(status().isOk());

        verify(employerApplicationService, times(1)).updateApplicationStatus(anyLong(), any());
    }

    @Test
    @WithMockUser(roles = "EMPLOYER")
    void testGetApplicationsByJobPosting_Success() throws Exception {
        List<ApplicationResponseDTO> applicationList = new ArrayList<>();
        applicationList.add(applicationResponseDTO);

        PaginatedResponse<ApplicationResponseDTO> paginatedResponse = PaginatedResponse.<ApplicationResponseDTO>builder()
                .content(applicationList)
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1)
                .totalPages(1)
                .isLast(true)
                .build();

        when(employerApplicationService.getApplicationsByJobPosting(anyLong(), anyLong(), any()))
                .thenReturn(paginatedResponse);

        mockMvc.perform(get("/api/v1/employers/applications/jobs/1")
                                .with(employerAuth())
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(employerApplicationService, times(1)).getApplicationsByJobPosting(anyLong(), anyLong(), any());
    }
    private RequestPostProcessor employerAuth() {
        CustomUserDetails userDetails = CustomUserDetails.builder()
                                                .userId(1L)
                                                .username("employer@test.com")
                                                .password("password")
                                                .fullName("Tech Company")
                                                .email("employer@test.com")
                                                .isActive(true)
                                                .authorities(List.of(new SimpleGrantedAuthority("ROLE_EMPLOYER")))
                                                .build();

        return authentication(new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        ));
    }
}

