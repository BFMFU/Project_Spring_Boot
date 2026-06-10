package app.product.project.service.impl;

import app.product.project.model.dto.request.UserDTO;
import app.product.project.model.dto.request.UserLogin;
import app.product.project.model.dto.response.JWTResponse;
import app.product.project.model.dto.response.UserResponseDTO;
import app.product.project.model.entity.Role;
import app.product.project.model.entity.Users;
import app.product.project.repository.RoleRepository;
import app.product.project.repository.UserRepository;
import app.product.project.security.jwt.JWTProvider;
import app.product.project.security.principal.CustomUserDetails;
import app.product.project.service.RefreshTokenService;
import app.product.project.service.TokenBlacklistService;
import app.product.project.service.UserService;
import app.product.project.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JWTProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;
	private final TokenBlacklistService tokenBlacklistService;
	private final FileUploadService fileUploadService;

	@Override
	public Users registerUser(UserDTO userDTO) {
		// Validate required fields
		if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
			log.error("Email is required for user registration");
			throw new IllegalArgumentException("Email không được để trống");
		}

		// Debug logging to check roleIds from request
		log.info("UserDTO received - roleIds: {}, roleIds is null: {}, isEmpty: {}",
				userDTO.getRoleIds(),
				userDTO.getRoleIds() == null,
				userDTO.getRoleIds() != null ? userDTO.getRoleIds().isEmpty() : "N/A");

		List<Role> roles = new ArrayList<>();
		if (userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
			log.info("Attempting to assign roles with IDs: {}", userDTO.getRoleIds());
			roles = roleRepository.findAllById(userDTO.getRoleIds());

			// If no roles found for the provided IDs, throw error
			if (roles.isEmpty()) {
				log.error("No roles found for the provided role IDs: {}", userDTO.getRoleIds());
				throw new RuntimeException("Không tìm thấy vai trò với ID được cung cấp. Vui lòng kiểm tra lại ID vai trò.");
			}

			// Filter out ADMIN role - prevent registration with admin privilege
			roles = roles.stream()
					.filter(role -> !role.getRoleName().equalsIgnoreCase("ADMIN"))
					.collect(Collectors.toList());

			// If all roles were ADMIN, assign default CANDIDATE role
			if (roles.isEmpty()) {
				log.warn("User attempted to register with ADMIN role. Assigning default CANDIDATE role to user: {}", userDTO.getUsername());
				Role candidateRole = roleRepository.findByRoleName("CANDIDATE")
						.orElseThrow(() -> {
							log.error("Default CANDIDATE role not found in database");
							return new RuntimeException("Vai trò CANDIDATE không tồn tại. Vui lòng khởi tạo dữ liệu ban đầu.");
						});
				roles = Collections.singletonList(candidateRole);
			} else {
				log.info("Successfully assigned {} role(s) to user: {}", roles.size(), userDTO.getUsername());
			}
		} else {
			// Assign default CANDIDATE role when no roles are provided
			log.info("No roleIds provided, assigning default CANDIDATE role to user: {}", userDTO.getUsername());
			Role candidateRole = roleRepository.findByRoleName("CANDIDATE")
					.orElseThrow(() -> {
						log.error("Default CANDIDATE role not found in database");
						return new RuntimeException("Vai trò CANDIDATE không tồn tại. Vui lòng khởi tạo dữ liệu ban đầu.");
					});
			roles = Collections.singletonList(candidateRole);
			log.info("Assigned default CANDIDATE role to user: {}", userDTO.getUsername());
		}

		Users users = Users.builder()
				              .username(userDTO.getUsername())
				              .password(passwordEncoder.encode(userDTO.getPassword()))
				              .fullName(userDTO.getFullName())
				              .email(userDTO.getEmail())
				              .isActive(true)
				              .roles(roles)
				              .build();

		Users savedUser = userRepository.save(users);
		log.info("User registered successfully with {} assigned role(s)", savedUser.getRoles().size());
		return savedUser;
	}

	@Override
	public JWTResponse login(UserLogin userLogin) {
		try{
			Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userLogin.getUsername(), userLogin.getPassword()));
			CustomUserDetails userDetails = (CustomUserDetails) authenticate.getPrincipal();

			assert userDetails != null;
			String token = jwtProvider.generateToken(userDetails.getUserId(), userDetails.getUsername(), userDetails.getAuthorities());
			String refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername()).getToken();
			return JWTResponse.builder()
					       .userId(userDetails.getUserId())
					       .username(userDetails.getUsername())
					       .fullName(userDetails.getFullName())
					       .isActive(userDetails.getIsActive())
					       .authorities(userDetails.getAuthorities())
					       .token(token)
					       .refreshToken(refreshToken)
					       .build();
		} catch (AuthenticationException e) {
			log.error("Lỗi xác thực: {}", e.getMessage());
			throw e;
		}
	}

	@Override
	public List<Users> getAllUsers() {
		return userRepository.findAll();
	}

	@Override
	public void logout(String token, String username) {
		try {
			tokenBlacklistService.blacklistToken(token, username);
			log.info("Token của người dùng {} đã được thêm vào danh sách đen", username);
		} catch (Exception e) {
			log.error("Lỗi khi đăng xuất: {}", e.getMessage());
			throw new RuntimeException("Đăng xuất thất bại");
		}
	}

	@Override
	public String getUsernameFromToken(String token) {
		return jwtProvider.getUsernameFromToken(token);
	}

	@Override
	public UserResponseDTO uploadCVFile(Long userId, MultipartFile cvFile) {
		try {
			log.info("Starting CV upload for user ID: {}", userId);

			// Upload file to Cloudinary
			String cvUrl = fileUploadService.uploadFileToCloud(cvFile);

			// Get user from database
			Users user = userRepository.findById(userId)
					.orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

			// Update user's CV URL
			user.setCvUrl(cvUrl);
			Users updatedUser = userRepository.save(user);

			log.info("CV uploaded successfully for user {}: {}", userId, cvUrl);

			// Convert to DTO
			return UserResponseDTO.builder()
					.userId(updatedUser.getUserId())
					.username(updatedUser.getUsername())
					.fullName(updatedUser.getFullName())
					.email(updatedUser.getEmail())
					.isActive(updatedUser.getIsActive())
					.cvUrl(updatedUser.getCvUrl())
					.roles(updatedUser.getRoles().stream()
							.map(role -> role.getRoleName())
							.collect(Collectors.toList()))
					.build();

		} catch (Exception e) {
			log.error("Error uploading CV for user {}: {}", userId, e.getMessage());
			throw e;
		}
	}
}
