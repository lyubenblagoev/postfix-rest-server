package com.lyubenblagoev.postfixrest.service.model;

import java.util.Date;

public class AliasResource {

	private Long id;
	private String alias;
	private String email;
	private Date created;
	private Date updated;
	private boolean enabled;
	
	public AliasResource() {
	}

	public AliasResource(Long id, String alias, String email) {
		this.id = id;
		this.alias = alias;
		this.email = email;
	}

	public AliasResource(Long id, String alias, String email, boolean enabled, Date created, Date updated) {
		this.id = id;
		this.alias = alias;
		this.email = email;
		this.enabled = enabled;
		this.created = created;
		this.updated = updated;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
