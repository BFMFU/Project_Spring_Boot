package app.product.project.service;

import app.product.project.model.dto.request.ApplicationRequestDTO;
import app.product.project.model.dto.response.ApplicationResponseDTO;

public interface CandidateApplicationService {
    ApplicationResponseDTO submitApplication(Long candidateId, ApplicationRequestDTO requestDTO);
}

