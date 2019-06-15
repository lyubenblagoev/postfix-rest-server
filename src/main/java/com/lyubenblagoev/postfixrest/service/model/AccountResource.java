package com.lyubenblagoev.postfixrest.service.model;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyubenblagoev.postfixrest.entity.Account;
import com.lyubenblagoev.postfixrest.entity.Domain;

public class AccountResource {

	private Long id;
	
	private String username;
	
	private String domain;
	
	private Long domainId;
	
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
	private Date created;
	
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
	private Date updated;
	
	private boolean enabled;
	
	public static AccountResource fromAccount(Account account) {
		AccountResource resource = new AccountResource();
		resource.setId(account.getId());
		resource.setUsername(account.getUsername());
		resource.setDomain(account.getDomain().getName());
		resource.setDomainId(account.getDomain().getId());
		resource.setEnabled(account.isEnabled());
		resource.setCreated(account.getCreated());
		resource.setUpdated(account.getUpdated());
		return resource;
	}
	
	public static Account toAccount(AccountResource account, Domain domain) {
		Account result = new Account();
		result.setId(account.getId());
		result.setUsername(account.getUsername());
		result.setEnabled(account.isEnabled());
		result.setCreated(account.getCreated());
		result.setUpdated(account.getUpdated());
		result.setDomain(domain);
		return result;
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
