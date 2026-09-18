package com.lyubenblagoev.postfixrest.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.List;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final String token;

    public JwtAuthenticationToken(String token) {
        super(List.of());
        this.token = token;
        setAuthenticated(false);
    }

    public String getToken() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return "";
    }

    @Override
    public Object getCredentials() {
        return "";
    }
}
