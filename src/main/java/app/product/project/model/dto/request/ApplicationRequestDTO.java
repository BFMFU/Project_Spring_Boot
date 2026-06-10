package app.product.project.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ApplicationRequestDTO {
    @NotNull(message = "Job ID không được để trống")
    private Long jobId;

    @NotBlank(message = "Cover letter không được để trống")
    private String coverLetter;

    @NotBlank(message = "CV URL không được để trống")
    private String cvUrl;
}

