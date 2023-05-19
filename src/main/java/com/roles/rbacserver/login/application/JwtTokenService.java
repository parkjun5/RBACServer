package com.roles.rbacserver.login.application;

import com.roles.rbacserver.login.exception.IllegalTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Slf4j
@Service
public class JwtTokenService {
    public static final String NOT_VALID_TOKEN_ERROR = "잘못된 토큰입니다. 재 로그인 해주세요.";
    private static final long EXPIRATION_TIME = 12;
    private final Key secretKey;

    public JwtTokenService(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(String name) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusHours(EXPIRATION_TIME);
        String token = Jwts.builder()
                .setSubject(name)
                .setIssuedAt(Timestamp.valueOf(now))
                .setExpiration(Timestamp.valueOf(expiryDate))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();

        return "Bearer " + token;
    }

    public boolean validateToken(String jwtToken) {
        try {
            parseJwt(jwtToken);
            return true;
        } catch (ExpiredJwtException e) {
            throw new IllegalTokenException(NOT_VALID_TOKEN_ERROR, e);
        } catch (Exception e) {
            log.error(jwtToken + NOT_VALID_TOKEN_ERROR, e);
        }
        return false;
    }

    public String getNameFromToken(String token) {
        return parseJwt(token).getSubject();
    }

    private Claims parseJwt(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}