package app.product.project.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UserResponseDTO {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private Boolean isActive;
    private String cvUrl;
    private List<String> roles;
}

