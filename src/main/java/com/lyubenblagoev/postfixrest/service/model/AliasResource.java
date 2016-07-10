package com.lyubenblagoev.postfixrest.service.model;

public class AliasResource {

	private Long id;
	private String alias;
	private String email;
	
	public AliasResource() {
	}

	public AliasResource(Long id, String alias, String email) {
		this.id = id;
		this.alias = alias;
		this.email = email;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
