package com.lyubenblagoev.postfixrest.service.model;

import java.util.Date;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

public class BccResource {

	private Long id;
	private Long accountId;
	@NotBlank @Email private String email;
	private boolean enabled;
	private Date created;
	private Date updated;
	
	public BccResource() {
	}

	public BccResource(Long id, Long accountId, String email, boolean enabled, Date created, Date updated) {
		this.id = id;
		this.accountId = accountId;
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

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
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

}
