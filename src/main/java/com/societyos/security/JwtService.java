package com.societyos.security;

import com.societyos.config.JwtConfigProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private final JwtConfigProperties jwtConfig;

    public JwtService(JwtConfigProperties jwtConfig) {
        this.jwtConfig = jwtConfig;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(jwtConfig.getSecret());
        } catch (Exception e) {
            keyBytes = jwtConfig.getSecret().getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserPrincipal principal) {
        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getExpirationMs());

        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("userId", principal.getId())
                .claim("email", principal.getEmail())
                .claim("roles", roles)
                .issuer(jwtConfig.getIssuer())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        Object userId = claims.get("userId");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("roles", List.class);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public long getExpirationMs() {
        return jwtConfig.getExpirationMs();
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw e;
        }
    }
}
