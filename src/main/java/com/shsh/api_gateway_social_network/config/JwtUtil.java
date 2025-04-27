package com.shsh.api_gateway_social_network.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    public String jwtSecret;

    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token");
        } catch (Exception e) {
            logger.error("Error extracting claims: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid JWT token");
        }
    }

    public boolean validateToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Date expiration = claims.getExpiration();
            Date now = new Date();
            logger.debug("Current time: {}", now);
            logger.debug("Token expiration time: {}", expiration);
            if (expiration.before(now)) {
                logger.error("JWT token is expired. Expiration: {}, Current time: {}", expiration, now);
                return false;
            }
            logger.debug("Token is valid. Expiration: {}, Current time: {}", expiration, now);
            return true;
        } catch (SignatureException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("Error validating token: {}", e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

}
