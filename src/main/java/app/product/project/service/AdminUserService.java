package app.product.project.service;

import app.product.project.model.dto.request.UserManagementRequestDTO;
import app.product.project.model.dto.response.PaginatedResponse;
import app.product.project.model.dto.response.UserResponseDTO;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {
    UserResponseDTO createUser(UserManagementRequestDTO requestDTO);

    UserResponseDTO updateUser(Long userId, UserManagementRequestDTO requestDTO);

    PaginatedResponse<UserResponseDTO> getAllUsers(Pageable pageable);

    UserResponseDTO getUserById(Long userId);

    void deactivateUser(Long userId);

    void activateUser(Long userId);

    void deleteUser(Long userId);
}

