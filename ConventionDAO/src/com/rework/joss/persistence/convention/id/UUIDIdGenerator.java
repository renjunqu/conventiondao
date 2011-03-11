package com.rework.joss.persistence.convention.id;

import com.rework.utils.UUIDHexGenerator;

public class UUIDIdGenerator implements IdGenerator{

	public Object id() {
		return UUIDHexGenerator.get();
	}
	
}
