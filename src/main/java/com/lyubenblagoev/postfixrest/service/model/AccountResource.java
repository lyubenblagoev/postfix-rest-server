package com.lyubenblagoev.postfixrest.service.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public class AccountResource {

	private Long id;
	
	private String username;
	
	private String domain;
	
	private Long domainId;
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date created;
	
	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date updated;
	
	private boolean enabled;
	
	public AccountResource() {
	}

	public AccountResource(Long id, String username, String domain, Long domainId) {
		this.id = id;
		this.username = username;
		this.domain = domain;
		this.domainId = domainId;
	}

	public AccountResource(Long id, String username, String domain, Long domainId, boolean enabled, Date created, Date updated) {
		this.id = id;
		this.username = username;
		this.domain = domain;
		this.domainId = domainId;
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getDomain() {
		return domain;
	}
	
	public void setDomain(String domainName) {
		this.domain = domainName;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
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
