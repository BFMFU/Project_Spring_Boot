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
public class UpdateApplicationStatusRequest {
    @NotNull(message = "Application ID không được để trống")
    private Long applicationId;

    @NotBlank(message = "Trạng thái ứng tuyển không được để trống")
    private String status;

    private String feedback;
}

