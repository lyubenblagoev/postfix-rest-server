package com.lyubenblagoev.postfixrest.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    @Value("${users.jwt.secret-key:change-this-default-jwt-secret-key-before-production}")
    private String secretKey = "change-this-default-jwt-secret-key-before-production";

    @Value("${users.jwt.expire-after-millis:900000}")
    private long validityInMilliseconds = 900000;

    public String createToken(String username, Collection<? extends GrantedAuthority> authorities) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        return Jwts.builder()
                .setSubject(username)
                .claim("auth", authorities.stream().map(a -> a.getAuthority()).collect(Collectors.toList()))
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(signingKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getUsername(String token) {
        return Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token)
                .getBody()
                .getSubject();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

}
