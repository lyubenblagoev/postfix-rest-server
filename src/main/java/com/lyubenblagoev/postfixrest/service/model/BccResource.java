package com.lyubenblagoev.postfixrest.service.model;

import java.util.Date;

import javax.validation.constraints.Email;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyubenblagoev.postfixrest.entity.Account;
import com.lyubenblagoev.postfixrest.entity.Bcc;

public class BccResource {

	private Long id;
	
	private Long accountId;
	
	@Email private String email;
	private boolean enabled;
	
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
	private Date created;
	
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
	private Date updated;
	
	public static BccResource fromBcc(Bcc bcc) {
		BccResource result = new BccResource();
		result.setId(bcc.getId());
		result.setAccountId(bcc.getAccount().getId());
		result.setEmail(bcc.getReceiverEmailAddress());
		result.setEnabled(bcc.isEnabled());
		result.setCreated(bcc.getCreated());
		result.setUpdated(bcc.getUpdated());
		return result;
	}
	
	public static Bcc toBcc(BccResource bcc, Account account) {
		Bcc result = new Bcc();
		result.setId(bcc.getId());
		result.setAccount(account);
		result.setReceiverEmailAddress(bcc.getEmail());
		result.setEnabled(bcc.isEnabled());
		result.setCreated(bcc.getCreated());
		result.setUpdated(bcc.getUpdated());
		return result;
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
