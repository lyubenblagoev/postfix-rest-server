package com.lyubenblagoev.postfixrest.entity;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class Bcc extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "account_id")
	private Account account;
	private String receiverEmailAddress;

	public Bcc() {
		super();
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public String getReceiverEmailAddress() {
		return receiverEmailAddress;
	}

	public void setReceiverEmailAddress(String receiverEmailAddress) {
		this.receiverEmailAddress = receiverEmailAddress;
	}

}
