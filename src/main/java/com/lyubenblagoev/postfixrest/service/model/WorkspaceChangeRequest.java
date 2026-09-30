package com.lyubenblagoev.postfixrest.service.model;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class WorkspaceChangeRequest {

    @NotBlank
    private String name;

    private List<String> domainNames;

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
}
