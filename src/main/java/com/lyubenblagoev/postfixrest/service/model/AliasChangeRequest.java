package com.lyubenblagoev.postfixrest.service.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;


public class AliasChangeRequest {
	
	private Long id;
	private Long domainId;
	private Boolean enabled;

	@NotEmpty
	@Pattern(regexp="^([a-z0-9]+((\\.|-)[a-z0-9]+)*)+$", message="must be a valid email prefix")
	private String name;

	@Email
	@NotEmpty
	private String email;

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
