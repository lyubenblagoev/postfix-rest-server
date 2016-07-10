package com.lyubenblagoev.postfixrest.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="domains")
public class Domain extends BaseEntity {
	
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
