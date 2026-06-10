package app.product.project.aspect;

import app.product.project.model.dto.response.ApplicationResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @AfterReturning(
            pointcut = "execution(* app.product.project.service.CandidateApplicationService.submitApplication(..))",
            returning = "result"
    )
    public void logApplicationSubmission(Object result) {
        if (result instanceof ApplicationResponseDTO applicationResponse) {
            log.info("[APPLICATION SUBMITTED] Candidate ID: {} ({}) applied for Job ID: {} ({}). Application ID: {}",
                    applicationResponse.getCandidateId(),
                    applicationResponse.getCandidateName(),
                    applicationResponse.getJobId(),
                    applicationResponse.getJobTitle(),
                    applicationResponse.getId());
        }
    }

    @AfterThrowing(
            pointcut = "execution(* app.product.project.service.CandidateApplicationService.submitApplication(..))",
            throwing = "exception"
    )
    public void logApplicationSubmissionError(JoinPoint joinPoint, Exception exception) {
        Object[] args = joinPoint.getArgs();
        if (args.length >= 1) {
            Long candidateId = (Long) args[0];
            log.error("[APPLICATION SUBMISSION FAILED] Candidate ID: {} - Error: {}",
                    candidateId, exception.getMessage(), exception);
        }
    }
}

