package com.rework.joss.persistence.test.biz;

import java.sql.Types;

import com.rework.core.dto.BaseObject;
import com.rework.joss.persistence.convention.annotation.DBColumn;

public class JoinTestDTO extends BaseObject{

	@DBColumn( columnName="id", columnType=Types.VARCHAR, length=32, primaryKey=true, nullable=false )
	private String testId;
	
	@DBColumn( columnType=Types.VARCHAR, length=100 )
	private String joinColumn;

	public String getTestId() {
		return testId;
	}

	public void setTestId(String testId) {
		this.testId = testId;
	}

	public String getJoinColumn() {
		return joinColumn;
	}

	public void setJoinColumn(String joinColumn) {
		this.joinColumn = joinColumn;
	}
	
}
