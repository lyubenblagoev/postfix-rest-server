package com.lyubenblagoev.postfixrest.entity;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name="domains")
public class Domain extends BaseEntity {

	@Column(nullable = false)
	private String name;

	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "domain")
	private List<Account> accounts = Collections.emptyList();

	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "domain")
	private List<Alias> aliases = Collections.emptyList();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Account> getAccounts() {
		return accounts;
	}

	public void setAccounts(List<Account> accounts) {
		this.accounts = accounts;
	}

	public List<Alias> getAliases() {
		return aliases;
	}

	public void setAliases(List<Alias> aliases) {
		this.aliases = aliases;
	}
}
