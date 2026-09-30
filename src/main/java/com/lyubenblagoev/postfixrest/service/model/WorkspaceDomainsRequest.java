package com.lyubenblagoev.postfixrest.service.model;

import java.util.List;

public class WorkspaceDomainsRequest {

    private List<String> domainNames = List.of();

    public List<String> getDomainNames() {
        return domainNames;
    }

    public void setDomainNames(List<String> domainNames) {
        this.domainNames = domainNames;
    }
}
