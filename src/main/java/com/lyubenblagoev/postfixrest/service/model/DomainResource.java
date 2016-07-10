package com.lyubenblagoev.postfixrest.service.model;

import org.hibernate.validator.constraints.NotBlank;

public class DomainResource {

	private Long id;
	
	@NotBlank
	private String name;
	
	public DomainResource() {
	}
	
	public DomainResource(Long id, String name) {
		this.id = id;
		this.name = name;
	}

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

}
