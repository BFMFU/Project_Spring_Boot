package app.product.project.service.impl;

import app.product.project.model.entity.TokenBlacklist;
import app.product.project.repository.TokenBlacklistRepository;
import app.product.project.security.jwt.JWTProvider;
import app.product.project.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final JWTProvider jwtProvider;

    @Override
    public void blacklistToken(String token, String username) {
        try {
            Instant expiryDate = jwtProvider.getExpirationDateFromToken(token);
            TokenBlacklist blacklistedToken = TokenBlacklist.builder()
                    .token(token)
                    .username(username)
                    .expiryDate(expiryDate)
                    .blacklistedAt(Instant.now())
                    .build();
            tokenBlacklistRepository.save(blacklistedToken);
        } catch (Exception e) {
            throw new RuntimeException("Không thể thêm token vào danh sách đen: " + e.getMessage());
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.existsByToken(token);
    }
}

