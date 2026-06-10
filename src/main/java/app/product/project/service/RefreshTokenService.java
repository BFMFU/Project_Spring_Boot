package app.product.project.service;

import app.product.project.model.dto.request.RefreshTokenRequest;
import app.product.project.model.dto.response.JWTResponse;
import app.product.project.model.entity.RefreshToken;


public interface RefreshTokenService {
    RefreshToken createRefreshToken(String username);
    RefreshToken verifyExpiration(RefreshToken token);
    JWTResponse refreshToken(RefreshTokenRequest refreshTokenRequest);

}
