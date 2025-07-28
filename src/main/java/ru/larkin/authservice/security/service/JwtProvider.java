package ru.larkin.authservice.security.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ru.larkin.authservice.model.RefreshToken;
import ru.larkin.authservice.model.TokenType;
import ru.larkin.authservice.repository.TokenRepository;
import ru.larkin.authservice.exception.InvalidJwtTokenException;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class JwtProvider {

    private final SecretKey jwtAccessSecret;
    private final SecretKey jwtRefreshSecret;
    private final int expirationTimeAccessInMinutes;
    private final int expirationTimeRefreshInDays;
    private final TokenRepository tokenRepository;
    private final ObjectMapper objectMapper;

    public JwtProvider(
            @Value("${jwt.secret.access}") String jwtAccessSecret,
            @Value("${jwt.secret.refresh}") String jwtRefreshSecret,
            @Value("${jwt.expiration-time.access}") int expirationTimeAccessInMinutes,
            @Value("${jwt.expiration-time.refresh}") int expirationTimeRefreshInDays,
            TokenRepository tokenRepository) {
        this.jwtAccessSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtAccessSecret));
        this.jwtRefreshSecret = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtRefreshSecret));
        this.expirationTimeAccessInMinutes = expirationTimeAccessInMinutes;
        this.expirationTimeRefreshInDays = expirationTimeRefreshInDays;
        this.tokenRepository = tokenRepository;
        this.objectMapper = new ObjectMapper();
    }

    public String generateAccessToken(UserDetails userDetails) {
        Date accessExpiration = Date.from(
                LocalDateTime.now()
                        .plusMinutes(expirationTimeAccessInMinutes)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .claim("roles", roles)
                .expiration(accessExpiration)
                .signWith(jwtAccessSecret)
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Date refreshExpiration = Date.from(
                LocalDateTime.now()
                        .plusDays(expirationTimeRefreshInDays)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
        );
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(refreshExpiration)
                .signWith(jwtRefreshSecret)
                .compact();
    }

    public Claims extractAccessClaims(String token) {
        return extractClaims(token, jwtAccessSecret);
    }

    public Claims extractRefreshClaims(String token) {
        return extractClaims(token, jwtRefreshSecret);
    }

    public String extractUsername(String token, TokenType tokenType) {
        if (tokenType.equals(TokenType.ACCESS)) {
            Claims claims = extractAccessClaims(token);
            return claims.getSubject();
        } else if (tokenType.equals(TokenType.REFRESH)) {
            Claims claims = extractRefreshClaims(token);
            return claims.getSubject();
        } else {
            throw new InvalidJwtTokenException("Invalid token type");
        }
    }

    public List<String> extractRoles(String token) {
        Claims claims = extractAccessClaims(token);
        return objectMapper.convertValue(claims.get("roles", List.class),
                new TypeReference<List<String>>() {});
    }

    public Instant extractExpiration(String refreshToken) {
        return extractRefreshClaims(refreshToken).getExpiration().toInstant();
    }

    private Claims extractClaims(String token, SecretKey secret) {
        return Jwts.parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public void validateAccessToken(@NotNull String accessToken) {
        validateToken(accessToken, jwtAccessSecret, TokenType.ACCESS);
    }

    public void validateRefreshToken(@NotNull String refreshToken) {
        Optional<RefreshToken> token = tokenRepository.findRefreshTokenByToken(refreshToken);
        if (token.isEmpty()) {
            throw new InvalidJwtTokenException("Invalid Refresh token");
        }
        if (token.get().isRevoked()) {
            throw new InvalidJwtTokenException("Refresh token is revoked");
        }
        validateToken(refreshToken, jwtRefreshSecret, TokenType.REFRESH);
    }

    private void validateToken(@NotNull String token, @NotNull SecretKey secret, TokenType tokenType) {
        try {
            Jwts.parser()
                    .verifyWith(secret)
                    .build()
                    .parseSignedClaims(token);
        } catch (ExpiredJwtException expEx) {
            throw new InvalidJwtTokenException(tokenType.getValue() + " token expired");
        } catch (UnsupportedJwtException | SignatureException unsEx) {
            throw new InvalidJwtTokenException("Invalid " + tokenType.getValue() + " token signature");
        } catch (MalformedJwtException mjEx) {
            throw new InvalidJwtTokenException("Malformed " + tokenType.getValue() + " token");
        } catch (Exception e) {
            throw new InvalidJwtTokenException("Invalid " + tokenType.getValue() + " token");
        }
    }
}
