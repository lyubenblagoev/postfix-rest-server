package com.lyubenblagoev.postfixrest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.List;

@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_WORKSPACE_ADMIN = "WORKSPACE_ADMIN";
    public static final String ROLE_WORKSPACE_MEMBER = "WORKSPACE_MEMBER";

    @Column
    private String name;

    @ManyToMany(mappedBy = "roles")
    private List<User> users;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
