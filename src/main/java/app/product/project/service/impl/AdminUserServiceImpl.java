package app.product.project.service.impl;

import app.product.project.model.dto.request.UserManagementRequestDTO;
import app.product.project.model.dto.response.PaginatedResponse;
import app.product.project.model.dto.response.UserResponseDTO;
import app.product.project.model.entity.Role;
import app.product.project.model.entity.Users;
import app.product.project.repository.RoleRepository;
import app.product.project.repository.UserRepository;
import app.product.project.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminUserServiceImpl implements AdminUserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDTO createUser(UserManagementRequestDTO requestDTO) {
        log.info("Creating new user: {}", requestDTO.getUsername());

        // Fetch roles using Stream API
        List<Role> roles = requestDTO.getRoleIds()
                .stream()
                .map(roleRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toList());

        Users user = Users.builder()
                .username(requestDTO.getUsername())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .fullName(requestDTO.getFullName())
                .email(requestDTO.getEmail())
                .isActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : true)
                .roles(roles)
                .build();

        Users savedUser = userRepository.save(user);
        log.info("User created successfully: {}", savedUser.getUserId());
        return toUserResponseDTO(savedUser);
    }

    @Override
    public UserResponseDTO updateUser(Long userId, UserManagementRequestDTO requestDTO) {
        log.info("Updating user: {}", userId);

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new RuntimeException("Người dùng không tồn tại");
                });

        // Fetch roles using Stream API
        List<Role> roles = requestDTO.getRoleIds()
                .stream()
                .map(roleRepository::findById)
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .collect(Collectors.toList());

        user.setUsername(requestDTO.getUsername());
        user.setPassword(passwordEncoder.encode(requestDTO.getPassword()));
        user.setFullName(requestDTO.getFullName());
        user.setEmail(requestDTO.getEmail());
        user.setIsActive(requestDTO.getIsActive() != null ? requestDTO.getIsActive() : user.getIsActive());
        user.setRoles(roles);

        Users updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", userId);
        return toUserResponseDTO(updatedUser);
    }

    @Override
    public PaginatedResponse<UserResponseDTO> getAllUsers(Pageable pageable) {
        log.info("Fetching all users with pagination - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

        Page<Users> usersPage = userRepository.findAll(pageable);

        // Using Stream API to map entities to DTOs
        List<UserResponseDTO> userDTOs = usersPage.getContent()
                .stream()
                .map(this::toUserResponseDTO)
                .collect(Collectors.toList());

        log.info("Retrieved {} users", userDTOs.size());
        return PaginatedResponse.<UserResponseDTO>builder()
                .content(userDTOs)
                .pageNumber(usersPage.getNumber())
                .pageSize(usersPage.getSize())
                .totalElements(usersPage.getTotalElements())
                .totalPages(usersPage.getTotalPages())
                .isLast(usersPage.isLast())
                .build();
    }

    @Override
    public UserResponseDTO getUserById(Long userId) {
        log.info("Fetching user: {}", userId);

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new RuntimeException("Người dùng không tồn tại");
                });

        return toUserResponseDTO(user);
    }

    @Override
    public void deactivateUser(Long userId) {
        log.info("Deactivating user: {}", userId);

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new RuntimeException("Người dùng không tồn tại");
                });

        user.setIsActive(false);
        userRepository.save(user);
        log.info("User deactivated successfully: {}", userId);
    }

    @Override
    public void activateUser(Long userId) {
        log.info("Activating user: {}", userId);

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found: {}", userId);
                    return new RuntimeException("Người dùng không tồn tại");
                });

        user.setIsActive(true);
        userRepository.save(user);
        log.info("User activated successfully: {}", userId);
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user: {}", userId);

        if (!userRepository.existsById(userId)) {
            log.error("User not found: {}", userId);
            throw new RuntimeException("Người dùng không tồn tại");
        }

        userRepository.deleteById(userId);
        log.info("User deleted successfully: {}", userId);
    }

    private UserResponseDTO toUserResponseDTO(Users user) {
        List<String> roleNames = user.getRoles()
                .stream()
                .map(Role::getRoleName)
                .collect(Collectors.toList());

        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .isActive(user.getIsActive())
                .roles(roleNames)
                .build();
    }
}

