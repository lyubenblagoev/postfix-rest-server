package com.lyubenblagoev.postfixrest.service.model;

import jakarta.validation.constraints.NotBlank;

public class WorkspaceDomainRequest {

    @NotBlank
    private String domainName;

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }
}
