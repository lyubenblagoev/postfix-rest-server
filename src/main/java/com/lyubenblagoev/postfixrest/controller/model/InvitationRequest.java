package com.lyubenblagoev.postfixrest.controller.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class InvitationRequest {

    @NotBlank
    @Email
    private String email;

    private InvitationRole role;

    private Long workspaceId;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public InvitationRole getRole() {
        return role;
    }

    public void setRole(InvitationRole role) {
        this.role = role;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }
}
