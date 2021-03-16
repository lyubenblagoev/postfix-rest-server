package com.lyubenblagoev.postfixrest.controller.model;

import com.lyubenblagoev.postfixrest.service.model.PasswordConfirmable;
import com.lyubenblagoev.postfixrest.service.model.validation.PasswordsMatches;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

@PasswordsMatches
public class UserUpdateRequest implements PasswordConfirmable {

    @NotEmpty
    @Email
    private String email;

    private String password;

    private String passwordConfirmation;

    @NotBlank
    private String oldPassword;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }

    public void setPasswordConfirmation(String passwordConfirmation) {
        this.passwordConfirmation = passwordConfirmation;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }
}
