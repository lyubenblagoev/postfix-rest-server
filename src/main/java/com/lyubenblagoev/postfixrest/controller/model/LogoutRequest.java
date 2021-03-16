package com.lyubenblagoev.postfixrest.controller.model;

public class LogoutRequest {

    private String login;

    private String refreshToken;

    public String getLogin() {
        return login;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
