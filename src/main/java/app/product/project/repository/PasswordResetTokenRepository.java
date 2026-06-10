package app.product.project.repository;

import app.product.project.model.entity.PasswordResetToken;
import app.product.project.model.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByUserAndVerificationCodeAndIsUsedFalse(Users user, String verificationCode);
    Optional<PasswordResetToken> findByUserAndIsUsedFalseOrderByCreatedAtDesc(Users user);
    void deleteByExpiryDateBeforeAndIsUsedFalse(LocalDateTime expiryDate);
}

