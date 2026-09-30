package com.lyubenblagoev.postfixrest.service.model;

import java.util.Date;

public class InvitationResource {

    private String email;
    private String role;
    private Long workspaceId;
    private String token;
    private Date expiresAt;

    public InvitationResource(String email, String role, Long workspaceId, String token, Date expiresAt) {
        this.email = email;
        this.role = role;
        this.workspaceId = workspaceId;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public String getToken() {
        return token;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }
}
