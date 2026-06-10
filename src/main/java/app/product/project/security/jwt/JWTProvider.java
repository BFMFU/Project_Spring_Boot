package app.product.project.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JWTProvider {
    @Value("${jwt-secret}")
    private String jwtSecret;
    @Value("${jwt-expire}")
    private Long jwtExpired;


    public String generateToken(String username){
        try{
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Date now = new Date();
            Date expire = new Date(now.getTime()+jwtExpired);

            return Jwts.builder()
                    .subject(username)
                    .signWith(key)
                    .issuedAt(now)
                    .expiration(expire)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String generateToken(Long userId, String username, Collection<? extends GrantedAuthority> authorities){
        try{
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Date now = new Date();
            Date expire = new Date(now.getTime()+ jwtExpired);

            String roles = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            return Jwts.builder()
                    .subject(username)
                    .claim("userId", userId)
                    .claim("roles", roles)
                    .signWith(key)
                    .issuedAt(now)
                    .expiration(expire)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean validateToken(String token){
        try{
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (UnsupportedJwtException e) {
            log.debug("Hệ thống không hỗ trợ jwt: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.debug("Chuỗi jwt không đúng: {}", e.getMessage());
            return false;
        } catch (ExpiredJwtException e){
            log.debug("Chuỗi jwt hết hạn: {}", e.getMessage());
            return false;
        } catch (SignatureException e){
            log.debug("Sai chữ ký JWT: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e){
            log.debug("Chuỗi JWT rỗng: {}", e.getMessage());
            return false;
        } catch (JwtException e){
            log.debug("JWT không hợp lệ: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token){
        try{
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Không lấy được username từ chuỗi token");
        }
    }

    public java.time.Instant getExpirationDateFromToken(String token){
        try{
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            Date expirationDate = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getExpiration();
            return expirationDate.toInstant();
        } catch (Exception e) {
            throw new RuntimeException("Không lấy được ngày hết hạn từ chuỗi token");
        }
    }
}
