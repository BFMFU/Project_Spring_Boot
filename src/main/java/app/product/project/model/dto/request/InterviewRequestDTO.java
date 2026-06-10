package app.product.project.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class InterviewRequestDTO {
    @NotNull(message = "Ngày phỏng vấn không được để trống")
    private LocalDateTime scheduledDate;

    @NotBlank(message = "Loại phỏng vấn không được để trống")
    private String interviewType;

    private String location;

    private String notes;

    private String feedback;
}

