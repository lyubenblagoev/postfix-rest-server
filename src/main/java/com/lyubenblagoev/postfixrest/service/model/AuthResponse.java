package com.lyubenblagoev.postfixrest.service.model;

import java.util.Date;

public class AuthResponse {

    private String token;

    private String refreshToken;

    private Date refreshTokenExpirationDate;

    public AuthResponse(String token, String refreshToken, Date refreshTokenExpirationDate) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.refreshTokenExpirationDate = refreshTokenExpirationDate;
    }

    public String getToken() {
        return token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Date getRefreshTokenExpirationDate() {
        return refreshTokenExpirationDate;
    }
}
