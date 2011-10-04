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
import java.sql.Types;
import java.util.List;

import com.rework.core.dto.BaseObject;
import com.rework.joss.persistence.convention.annotation.DBColumn;
import com.rework.joss.persistence.convention.annotation.Fetch;
import com.rework.joss.persistence.test.tigger.TiggerDAO;

public class DAOTestDTO extends BaseObject{
	
	private String id;
	private String notMatchId;
	private String testId;
	private String testname;
	private Integer testint;
	private Date testdate;
	
	
	@DBColumn(comment="新增加的字段", columnType = Types.VARCHAR, length = 200)
	private String newcolumn;
	
	@DBColumn(columnName="new_column2", length=10, comment="新增加的字段", defaultValue="20", columnType = Types.INTEGER)
	private Integer newcolumn2;
	
	private String bigtext;
	
	private List<JoinTestDTO> testlist;
	
	private String joinTestId;
	
	private Boolean testboolean;
	
	public Date getTestdate() {
		return testdate;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setTestdate(Date testdate) {
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

	public String getNewcolumn() {
		return newcolumn;
	}

	public void setNewcolumn(String newcolumn) {
		this.newcolumn = newcolumn;
	}

	public Integer getNewcolumn2() {
		return newcolumn2;
	}

	public void setNewcolumn2(Integer newcolumn2) {
		this.newcolumn2 = newcolumn2;
	}
	
	
}
