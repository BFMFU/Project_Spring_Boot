package app.product.project.service.impl;

import app.product.project.security.jwt.JWTProvider;
import app.product.project.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final JWTProvider jwtProvider;

    private static final String TOKEN_BLACKLIST_PREFIX = "token_blacklist:";
    private static final String USER_TOKENS_PREFIX = "user_tokens:";

    @Override
    public void blacklistToken(String token, String username) {
        try {
            Instant expiryDate = jwtProvider.getExpirationDateFromToken(token);
            String key = TOKEN_BLACKLIST_PREFIX + token;
            String userTokensKey = USER_TOKENS_PREFIX + username;

            // Calculate TTL in seconds
            long ttlSeconds = calculateTTL(expiryDate);

            // Store in Redis with automatic expiry
            redisTemplate.opsForValue().set(key, username, ttlSeconds, TimeUnit.SECONDS);

            // Keep track of user's blacklisted tokens for logout all functionality
            redisTemplate.opsForSet().add(userTokensKey, token);
            redisTemplate.expire(userTokensKey, ttlSeconds, TimeUnit.SECONDS);

            log.info("Token của người dùng {} đã được thêm vào danh sách đen trong Redis, TTL: {} giây", username, ttlSeconds);
        } catch (Exception e) {
            log.error("Lỗi khi thêm token vào danh sách đen: {}", e.getMessage());
            throw new RuntimeException("Không thể thêm token vào danh sách đen: " + e.getMessage());
        }
    }


    @Override
    public boolean isTokenBlacklisted(String token) {
        try {
            String key = TOKEN_BLACKLIST_PREFIX + token;
            Boolean hasToken = redisTemplate.hasKey(key);
            return Boolean.TRUE.equals(hasToken);
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra token trong danh sách đen: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void removeBlacklistByUsername(String username) {
        try {
            String userTokensKey = USER_TOKENS_PREFIX + username;
            Set<Object> userTokens = redisTemplate.opsForSet().members(userTokensKey);

            if (userTokens != null && !userTokens.isEmpty()) {
                // Delete all tokens for this user
                for (Object token : userTokens) {
                    String tokenKey = TOKEN_BLACKLIST_PREFIX + token;
                    redisTemplate.delete(tokenKey);
                }

                // Delete the user tokens set
                redisTemplate.delete(userTokensKey);
                log.info("Đã xóa tất cả token trong danh sách đen của người dùng {}", username);
            }
        } catch (Exception e) {
            log.error("Lỗi khi xóa danh sách đen của người dùng {}: {}", username, e.getMessage());
        }
    }

    private long calculateTTL(Instant expiryDate) {
        long expiryEpochSeconds = expiryDate.getEpochSecond();
        long nowEpochSeconds = Instant.now().getEpochSecond();
        long ttlSeconds = expiryEpochSeconds - nowEpochSeconds;

        // Ensure TTL is at least 1 second
        return Math.max(ttlSeconds, 1);
    }
}


