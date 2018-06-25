package com.lyubenblagoev.postfixrest.service.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;


public class AliasChangeRequest {
	
	private Long id;
	private Long domainId;
	@NotEmpty private String name;
	@Email @NotEmpty private String email;
	private Boolean enabled;
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

}
