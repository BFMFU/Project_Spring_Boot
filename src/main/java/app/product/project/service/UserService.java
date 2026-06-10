package app.product.project.service;

import app.product.project.model.dto.request.UserDTO;
import app.product.project.model.dto.request.UserLogin;
import app.product.project.model.dto.response.JWTResponse;
import app.product.project.model.dto.response.UserResponseDTO;
import app.product.project.model.entity.Users;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
	Users registerUser(UserDTO userDTO);
	JWTResponse login(UserLogin userLogin);
	List<Users> getAllUsers();
	void logout(String token, String username);
	String getUsernameFromToken(String token);
	UserResponseDTO uploadCVFile(Long userId, MultipartFile cvFile);
}
