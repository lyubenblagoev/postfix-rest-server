package com.lyubenblagoev.postfixrest.service.model;

import java.util.Date;

import org.hibernate.validator.constraints.NotBlank;

public class DomainResource {

	private Long id;
	private Date created;
	private Date updated;
	private Boolean enabled;
	
	@NotBlank
	private String name;
	
	public DomainResource() {
	}
	
	public DomainResource(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public DomainResource(Long id, String name, boolean enabled, Date created, Date updated) {
		this.id = id;
		this.name = name;
		this.enabled = enabled;
		this.created = created;
		this.updated = updated;
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

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

}
