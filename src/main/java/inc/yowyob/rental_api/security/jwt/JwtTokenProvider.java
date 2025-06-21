package inc.yowyob.rental_api.security.jwt;

import inc.yowyob.rental_api.config.AppProperties;
import inc.yowyob.rental_api.user.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final AppProperties appProperties;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(appProperties.getJwt().getSecret().getBytes());
    }

    /**
     * Génère un token JWT pour un utilisateur
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("email", user.getEmail());
        claims.put("userType", user.getUserType().getCode());
        claims.put("status", user.getStatus().getCode());

        if (user.getOrganizationId() != null) {
            claims.put("organizationId", user.getOrganizationId().toString());
        }

        return createToken(claims, user.getEmail(), appProperties.getJwt().getExpiration());
    }

    /**
     * Génère un refresh token
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        claims.put("type", "refresh");

        return createToken(claims, user.getEmail(), appProperties.getJwt().getRefreshExpiration());
    }

    /**
     * Crée un token JWT
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Extrait l'email du token
     */
    public String getEmailFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * Extrait l'ID utilisateur du token
     */
    public UUID getUserIdFromToken(String token) {
        String userId = (String) getClaimsFromToken(token).get("userId");
        return UUID.fromString(userId);
    }

    /**
     * Extrait l'ID organisation du token
     */
    public UUID getOrganizationIdFromToken(String token) {
        String orgId = (String) getClaimsFromToken(token).get("organizationId");
        return orgId != null ? UUID.fromString(orgId) : null;
    }

    /**
     * Extrait le type d'utilisateur du token
     */
    public String getUserTypeFromToken(String token) {
        return (String) getClaimsFromToken(token).get("userType");
    }

    /**
     * Extrait la date d'expiration du token
     */
    public Date getExpirationFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    /**
     * Extrait tous les claims du token
     */
    private Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    /**
     * Vérifie si le token a expiré
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Valide le token JWT
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Extrait le token du header Authorization
     */
    public String extractTokenFromHeader(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
