package app.product.project.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class JobPostingRequestDTO {
    @NotBlank(message = "Tiêu đề job không được để trống")
    private String title;

    @NotBlank(message = "Mô tả job không được để trống")
    private String description;

    @NotBlank(message = "Khoảng lương không được để trống")
    private String salaryRange;

    private Long employerId;

    private Long statusId;
}

