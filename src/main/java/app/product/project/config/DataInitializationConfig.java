package app.product.project.config;

import app.product.project.model.entity.ApplicationStatus;
import app.product.project.model.entity.JobStatus;
import app.product.project.model.entity.Role;
import app.product.project.model.enums.ApplicationStatusEnum;
import app.product.project.model.enums.JobStatusEnum;
import app.product.project.model.enums.RoleEnum;
import app.product.project.repository.ApplicationStatusRepository;
import app.product.project.repository.JobStatusRepository;
import app.product.project.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializationConfig {

    @Bean
    public CommandLineRunner initializeApplicationStatuses(ApplicationStatusRepository applicationStatusRepository) {
        return args -> {
            log.info("Initializing Application Statuses...");

            for (ApplicationStatusEnum statusEnum : ApplicationStatusEnum.values()) {
                // Check if status already exists
                if (applicationStatusRepository.findByStatusName(statusEnum.getValue()).isEmpty()) {
                    ApplicationStatus status = ApplicationStatus.builder()
                            .statusName(statusEnum.getValue())
                            .build();
                    applicationStatusRepository.save(status);
                    log.info("Created Application Status: {} - {}", statusEnum.getValue(), statusEnum.getDescription());
                } else {
                    log.debug("Application Status already exists: {}", statusEnum.getValue());
                }
            }

            log.info("Application Statuses initialization completed");
        };
    }

    @Bean
    public CommandLineRunner initializeRoles(RoleRepository roleRepository) {
        return args -> {
            log.info("Initializing Roles...");

            for (RoleEnum roleEnum : RoleEnum.values()) {
                // Check if role already exists
                if (roleRepository.findByRoleName(roleEnum.getValue()).isEmpty()) {
                    Role role = Role.builder()
                            .roleName(roleEnum.getValue())
                            .build();
                    roleRepository.save(role);
                    log.info("Created Role: {} - {}", roleEnum.getValue(), roleEnum.getDescription());
                } else {
                    log.debug("Role already exists: {}", roleEnum.getValue());
                }
            }

            log.info("Roles initialization completed");
        };
    }

    @Bean
    public CommandLineRunner initializeJobStatuses(JobStatusRepository jobStatusRepository) {
        return args -> {
            log.info("Initializing Job Statuses...");

            for (JobStatusEnum statusEnum : JobStatusEnum.values()) {
                // Check if job status already exists
                if (jobStatusRepository.findByStatusName(statusEnum.getValue()).isEmpty()) {
                    JobStatus jobStatus = JobStatus.builder()
                            .statusName(statusEnum.getValue())
                            .build();
                    jobStatusRepository.save(jobStatus);
                    log.info("Created Job Status: {} - {}", statusEnum.getValue(), statusEnum.getDescription());
                } else {
                    log.debug("Job Status already exists: {}", statusEnum.getValue());
                }
            }

            log.info("Job Statuses initialization completed");
        };
    }
}

