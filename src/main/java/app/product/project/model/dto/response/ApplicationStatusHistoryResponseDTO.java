package app.product.project.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ApplicationStatusHistoryResponseDTO {
    private Long id;
    private Long applicationId;
    private String oldStatus;
    private String newStatus;
    private String feedback;
    private Long changedById;
    private String changedByName;
    private LocalDateTime changedAt;
    private String reason;
}

