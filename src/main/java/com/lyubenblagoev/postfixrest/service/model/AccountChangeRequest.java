package com.lyubenblagoev.postfixrest.service.model;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import com.lyubenblagoev.postfixrest.service.model.validation.PasswordsMatches;

@PasswordsMatches
public class AccountChangeRequest implements PasswordConfirmable {

	private Long id;

	@NotEmpty
	@Pattern(regexp="^([a-z0-9]+((\\.|-)[a-z0-9]+)*)+$", message="must be a valid email prefix")
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

	@Override
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String getPasswordConfirmation() {
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
