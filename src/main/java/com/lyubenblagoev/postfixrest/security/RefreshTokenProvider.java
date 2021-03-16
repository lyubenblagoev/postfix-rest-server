package com.lyubenblagoev.postfixrest.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

@Component
public class RefreshTokenProvider {

    public class RefreshToken {

        private String token;

        private Date expirationDate;

        public RefreshToken(String token, Date expirationDate) {
            this.token = token;
            this.expirationDate = expirationDate;
        }

        public String getToken() {
            return token;
        }

        public Date getExpirationDate() {
            return expirationDate;
        }
    }

    @Value("${users.refresh-token.days-valid:30}")
    private Integer refreshTokenValidityInDays = 30;

    public RefreshToken createToken() {
        String refreshToken = UUID.randomUUID().toString();
        LocalDateTime expirationDateTime = LocalDateTime.now().plusDays(refreshTokenValidityInDays);
        Date expirationDate = Date.from(expirationDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return new RefreshToken(refreshToken, expirationDate);
    }
}
