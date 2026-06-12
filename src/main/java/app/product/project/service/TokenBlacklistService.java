package app.product.project.service;

public interface TokenBlacklistService {

    void blacklistToken(String token, String username);

    boolean isTokenBlacklisted(String token);

    void removeBlacklistByUsername(String username);
}

