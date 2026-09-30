package com.lyubenblagoev.postfixrest.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "workspaces")
public class Workspace extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @ManyToMany
    @JoinTable(name = "workspace_domains",
            joinColumns = @JoinColumn(name = "workspace_id"),
            inverseJoinColumns = @JoinColumn(name = "domain_id"))
    private List<Domain> domains = new ArrayList<>();

    @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkspaceMapping> mappings = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Domain> getDomains() {
        return domains;
    }

    public void setDomains(List<Domain> domains) {
        this.domains = domains;
    }

    public List<WorkspaceMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<WorkspaceMapping> mappings) {
        this.mappings = mappings;
    }
}
