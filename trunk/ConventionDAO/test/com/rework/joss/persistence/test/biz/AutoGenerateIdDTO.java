package com.rework.joss.persistence.test.biz;

import com.rework.core.dto.BaseObject;

public class AutoGenerateIdDTO extends BaseObject{
	
	private Integer id;
	
	private String name;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
