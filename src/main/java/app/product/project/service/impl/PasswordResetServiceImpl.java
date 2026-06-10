package app.product.project.service.impl;

import app.product.project.model.entity.PasswordResetToken;
import app.product.project.model.entity.Users;
import app.product.project.repository.PasswordResetTokenRepository;
import app.product.project.repository.UserRepository;
import app.product.project.service.EmailService;
import app.product.project.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${password.reset.token.expiry.minutes:30}")
    private Integer tokenExpiryMinutes;

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        // Check if user exists
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng với email: " + email + " không tồn tại"));

        // Invalidate any existing unused tokens
        Optional<PasswordResetToken> existingToken = passwordResetTokenRepository
                .findByUserAndIsUsedFalseOrderByCreatedAtDesc(user);
        if (existingToken.isPresent()) {
            PasswordResetToken token = existingToken.get();
            token.setIsUsed(true);
            passwordResetTokenRepository.save(token);
        }

        // Generate new 6-digit verification code
        String verificationCode = generateVerificationCode();

        // Create new password reset token
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .verificationCode(verificationCode)
                .expiryDate(LocalDateTime.now().plusMinutes(tokenExpiryMinutes))
                .isUsed(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Send email with verification code
        emailService.sendPasswordResetEmail(email, verificationCode);
        log.info("Password reset request processed for email: {}", email);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String verificationCode, String newPassword) {
        // Check if user exists
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Người dùng với email: " + email + " không tồn tại"));

        // Find valid token
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByUserAndVerificationCodeAndIsUsedFalse(user, verificationCode)
                .orElseThrow(() -> new RuntimeException("Mã xác nhận không hợp lệ hoặc đã hết hạn"));

        // Check if token has expired
        if (LocalDateTime.now().isAfter(resetToken.getExpiryDate())) {
            throw new RuntimeException("Mã xác nhận đã hết hạn. Vui lòng yêu cầu mã mới");
        }

        // Update password and mark token as used
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setIsUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);

        // Send success email
        emailService.sendPasswordResetSuccessEmail(email);
        log.info("Password reset successful for email: {}", email);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}

