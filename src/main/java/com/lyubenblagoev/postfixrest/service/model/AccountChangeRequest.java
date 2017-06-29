package com.lyubenblagoev.postfixrest.service.model;

import org.hibernate.validator.constraints.NotBlank;

import com.lyubenblagoev.postfixrest.service.model.validation.PasswordsMatches;

@PasswordsMatches
public class AccountChangeRequest {

	private Long id;

	@NotBlank
	private String username;

	private String password;
	private String confirmPassword;
	private Long domainId;
	private Boolean enabled;

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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getConfirmPassword() {
		return confirmPassword;
	}

	public void setConfirmPassword(String confirmPassword) {
		this.confirmPassword = confirmPassword;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

}
