package app.product.project.controller;

import app.product.project.model.dto.request.RefreshTokenRequest;
import app.product.project.model.dto.request.UserDTO;
import app.product.project.model.dto.request.UserLogin;
import app.product.project.model.dto.request.ForgotPasswordRequest;
import app.product.project.model.dto.request.VerifyCodeRequest;
import app.product.project.model.dto.response.ApiDataResponse;
import app.product.project.model.dto.response.JWTResponse;
import app.product.project.model.entity.Users;
import app.product.project.service.RefreshTokenService;
import app.product.project.service.UserService;
import app.product.project.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<ApiDataResponse<Users>> registerUser(@Valid @RequestBody UserDTO userDTO) {
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Đăng ký tài khoản " + userDTO.getUsername() + " thành công",
                userService.registerUser(userDTO),
                null,
                HttpStatus.CREATED
        ), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiDataResponse<JWTResponse>> login(@RequestBody UserLogin userLogin) {
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Đăng nhập thành công",
                userService.login(userLogin),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<ApiDataResponse<List<Users>>> getUsers() {
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Lấy danh sách tài khoản thành công",
                userService.getAllUsers(),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiDataResponse<JWTResponse>> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return new ResponseEntity<>(new ApiDataResponse<>(
                true,
                "Cấp lại access token mới thành công",
                refreshTokenService.refreshToken(refreshTokenRequest),
                null,
                HttpStatus.OK
        ), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiDataResponse<Void>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader != null && authHeader.startsWith("Bearer ")
            ? authHeader.substring(7)
            : null;

        if (token == null || token.trim().isEmpty()) {
            return new ResponseEntity<>(new ApiDataResponse<>(
                    false,
                    "Token không hợp lệ",
                    null,
                    null,
                    HttpStatus.BAD_REQUEST
            ), HttpStatus.BAD_REQUEST);
        }

        try {
            String username = userService.getUsernameFromToken(token);
            userService.logout(token, username);

            return new ResponseEntity<>(new ApiDataResponse<>(
                    true,
                    "Đăng xuất thành công",
                    null,
                    null,
                    HttpStatus.OK
            ), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiDataResponse<>(
                    false,
                    "Đăng xuất thất bại: " + e.getMessage(),
                    null,
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiDataResponse<Void>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.requestPasswordReset(request.getEmail());
            return new ResponseEntity<>(new ApiDataResponse<>(
                    true,
                    "Mã xác nhận đã được gửi đến email của bạn. Vui lòng kiểm tra email.",
                    null,
                    null,
                    HttpStatus.OK
            ), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(new ApiDataResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    null,
                    HttpStatus.BAD_REQUEST
            ), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiDataResponse<>(
                    false,
                    "Yêu cầu quên mật khẩu thất bại: " + e.getMessage(),
                    null,
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiDataResponse<Void>> resetPassword(@RequestBody VerifyCodeRequest request) {
        try {
            passwordResetService.resetPassword(request.getEmail(), request.getVerificationCode(), request.getNewPassword());
            return new ResponseEntity<>(new ApiDataResponse<>(
                    true,
                    "Mật khẩu đã được thay đổi thành công",
                    null,
                    null,
                    HttpStatus.OK
            ), HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(new ApiDataResponse<>(
                    false,
                    e.getMessage(),
                    null,
                    null,
                    HttpStatus.BAD_REQUEST
            ), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ApiDataResponse<>(
                    false,
                    "Đặt lại mật khẩu thất bại: " + e.getMessage(),
                    null,
                    null,
                    HttpStatus.INTERNAL_SERVER_ERROR
            ), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

