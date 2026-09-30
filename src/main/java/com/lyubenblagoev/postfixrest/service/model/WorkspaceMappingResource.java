package com.lyubenblagoev.postfixrest.service.model;

import com.lyubenblagoev.postfixrest.entity.WorkspaceMapping;

public class WorkspaceMappingResource {

    private Long id;
    private String email;
    private String role;

    public static WorkspaceMappingResource fromMapping(WorkspaceMapping mapping) {
        WorkspaceMappingResource resource = new WorkspaceMappingResource();
        resource.setId(mapping.getId());
        resource.setEmail(mapping.getUser().getEmail());
        resource.setRole(mapping.getRole().getName());
        return resource;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
