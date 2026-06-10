package app.product.project.service.impl;

import app.product.project.model.dto.request.RefreshTokenRequest;
import app.product.project.model.dto.response.JWTResponse;
import app.product.project.model.entity.RefreshToken;
import app.product.project.repository.RefreshTokenRepository;
import app.product.project.security.jwt.JWTProvider;
import app.product.project.security.principal.CustomUserDetails;
import app.product.project.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;




import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JWTProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    @Value("${jwt-refresh-expire}")
    private Long refreshExpired;

    @Override
    public RefreshToken createRefreshToken(String username) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpired))
                .username(username)
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    @Override
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            throw new RuntimeException("Refresh token đã hết hạn");
        }
        if (token.isRevoked()) {
            throw new RuntimeException("Refresh token đã bị thu hồi");
        }
        return token;
    }

    @Override
    public JWTResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String tokenStr = refreshTokenRequest.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new RuntimeException("Token không tồn tại"));

        verifyExpiration(refreshToken);

        String newAccessToken = jwtProvider.generateToken(refreshToken.getUsername());
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(refreshToken.getUsername());

        return JWTResponse.builder()
                .username(userDetails.getUsername())
                .fullName(userDetails.getFullName())
                .isActive(userDetails.getIsActive())
                .authorities(userDetails.getAuthorities())
                .token(newAccessToken)
                .build();
    }
}
