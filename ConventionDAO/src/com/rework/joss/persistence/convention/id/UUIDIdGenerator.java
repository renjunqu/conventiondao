package com.rework.joss.persistence.convention.id;

import javax.sql.DataSource;

import com.rework.utils.UUIDHexGenerator;

public class UUIDIdGenerator implements IdGenerator{

	public Object id(DataSource ds, String dbo) {
		return UUIDHexGenerator.get();
	}
	
	public String id() {
		return UUIDHexGenerator.get();
	}
}
