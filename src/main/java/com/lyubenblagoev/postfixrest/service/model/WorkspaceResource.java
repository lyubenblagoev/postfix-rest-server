package com.lyubenblagoev.postfixrest.service.model;

import com.lyubenblagoev.postfixrest.entity.Domain;
import com.lyubenblagoev.postfixrest.entity.Workspace;
import java.util.Date;
import java.util.List;

public class WorkspaceResource {

    private Long id;
    private String name;
    private List<String> domainNames;
    private Date updated;

    public static WorkspaceResource fromWorkspace(Workspace workspace) {
        WorkspaceResource resource = new WorkspaceResource();
        resource.setId(workspace.getId());
        resource.setName(workspace.getName());
        resource.setDomainNames(
                workspace.getDomains().stream()
                        .map(Domain::getName)
                        .sorted()
                        .toList());
        resource.setUpdated(workspace.getUpdated() == null ? workspace.getCreated() : workspace.getUpdated());
        return resource;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getDomainNames() {
        return domainNames;
    }

    public void setDomainNames(List<String> domainNames) {
        this.domainNames = domainNames;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }
}
