package com.lyubenblagoev.postfixrest.service.model;

public class AccountResource {

	private Long id;
	private String username;
	private String domain;
	private Long domainId;
	
	public AccountResource() {
	}

	public AccountResource(Long id, String username, String domain, Long domainId) {
		this.id = id;
		this.username = username;
		this.domain = domain;
		this.domainId = domainId;
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

}
