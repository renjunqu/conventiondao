package com.rework.joss.persistence.test.biz;

import com.rework.core.dto.BaseObject;
 
public class VwGyUserDTO extends BaseObject {
	private String id;
	private String username;
	private String xm;
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getXm() {
		return xm;
	}
	public void setXm(String xm) {
		this.xm = xm;
	}
}
