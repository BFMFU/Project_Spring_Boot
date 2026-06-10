package app.product.project.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class JobPostingResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String salaryRange;
    private Long employerId;
    private String employerName;
    private Long statusId;
    private String statusName;
}

