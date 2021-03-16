package com.lyubenblagoev.postfixrest.service.model;

public class RefreshTokenRequest {

    private String refreshToken;

    private String login;

    public RefreshTokenRequest(String refreshToken, String login) {
        this.refreshToken = refreshToken;
        this.login = login;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getLogin() {
        return login;
    }
}
