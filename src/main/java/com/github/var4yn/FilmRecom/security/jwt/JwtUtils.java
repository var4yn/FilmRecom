package com.github.var4yn.FilmRecom.security.jwt;

import com.github.var4yn.FilmRecom.security.services.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${filmrecom.app.jwtSecret}")
    private String jwtSecret;

    @Value("${filmrecom.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(authToken);
            logger.info("JWT токен валиден");
            return true;
        } catch (SecurityException e) {
            logger.error("Неверная подпись JWT токена: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.error("Неверный формат JWT токена: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT токен истек: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Неподдерживаемый JWT токен: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims строка пуста: {}", e.getMessage());
        }

        return false;
    }
} 