/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */ 
package com.rework.joss.persistence.test.biz;

import java.sql.Date;
import java.util.List;

import com.rework.core.dto.BaseObject;
import com.rework.joss.persistence.convention.annotation.Fetch;
import com.rework.joss.persistence.test.tigger.TiggerDAO;

public class DAOTestDateStringDTO extends BaseObject{
	
	private String id;
	private String notMatchId;
	private String testId;
	private String testname;
	private Integer testint;
	private String testdate;
	
	private String bigtext;
	
	private List<JoinTestDTO> testlist;
	
	private String joinTestId;
	
	private Boolean testboolean;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTestdate() {
		return testdate;
	}

	public void setTestdate(String testdate) {
		this.testdate = testdate;
	}

	public String getTestId() {
		return testId;
	}
	public void setTestId(String testId) {
		this.testId = testId;
	}
	public Integer getTestint() {
		return testint;
	}
	public void setTestint(Integer testint) {
		this.testint = testint;
	}
	public String getTestname() {
		return testname;
	}
	public void setTestname(String testname) {
		this.testname = testname;
	}
	
	@Fetch(dao=TiggerDAO.class, column="join_test_id")
	public List<JoinTestDTO> getTestlist() {
		return testlist;
	}
	
	public void setTestlist(List testlist) {
		this.testlist = testlist;
	}
	public String getNotMatchId() {
		return notMatchId;
	}
	public void setNotMatchId(String notMatchId) {
		this.notMatchId = notMatchId;
	}

	public Boolean getTestboolean() {
		return testboolean;
	}

	public void setTestboolean(Boolean testboolean) {
		this.testboolean = testboolean;
	}

	public String getJoinTestId() {
		return joinTestId;
	}

	public void setJoinTestId(String joinTestId) {
		this.joinTestId = joinTestId;
	}

	public String getBigtext() {
		return bigtext;
	}

	public void setBigtext(String bigtext) {
		this.bigtext = bigtext;
	}
	
	
}
