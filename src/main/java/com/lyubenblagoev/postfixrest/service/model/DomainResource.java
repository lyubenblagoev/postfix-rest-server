package com.lyubenblagoev.postfixrest.service.model;

import java.util.Date;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyubenblagoev.postfixrest.entity.Domain;

public class DomainResource {

	private Long id;
	
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
	private Date created;
	
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ")
	private Date updated;
	
	private Boolean enabled;
	
	@NotEmpty
	@Pattern(regexp="^([a-z0-9]+(-[a-z0-9]+)*\\.)+[a-z]{2,}$", message = "must be a valid domain name")
	private String name;
	
	public static DomainResource fromDomain(Domain domain) {
		DomainResource resource = new DomainResource();
		resource.setId(domain.getId());
		resource.setName(domain.getName());
		resource.setEnabled(domain.isEnabled());
		resource.setCreated(domain.getCreated());
		resource.setUpdated(domain.getUpdated());
		return resource;
	}
	
	public static Domain toDomain(DomainResource domain) {
		Domain d = new Domain();
		d.setId(domain.getId());
		d.setName(domain.getName());
		d.setEnabled(domain.getEnabled());
		d.setCreated(domain.getCreated());
		d.setUpdated(domain.getUpdated());
		return d;
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
