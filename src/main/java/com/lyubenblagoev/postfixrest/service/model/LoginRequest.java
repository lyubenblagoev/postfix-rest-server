package com.lyubenblagoev.postfixrest.service.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

public class LoginRequest {
    @NotEmpty @Email private String login;
    @NotEmpty private String password;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "user='" + login + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
