package com.heer.joss.persistence.test.biz;

import com.rework.core.dto.BaseObject;

public class JoinTestDTO extends BaseObject{

	private String testId;
	
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
