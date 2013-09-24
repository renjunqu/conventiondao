package com.rework.joss.persistence.test.biz;

import com.rework.core.dto.BaseObject;

public class ColumnDTO extends BaseObject {
	private String id;
	
	private String columnName;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	
}
