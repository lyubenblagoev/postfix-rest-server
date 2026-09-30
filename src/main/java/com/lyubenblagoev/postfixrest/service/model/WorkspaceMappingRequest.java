package com.lyubenblagoev.postfixrest.service.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class WorkspaceMappingRequest {

    @NotBlank
    @Email
    private String email;

    private String role;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
