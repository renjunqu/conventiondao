/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.rework.joss.persistence.test.testcase;

import java.util.List;

import com.rework.joss.persistence.convention.ConventionUtils;
import com.rework.joss.persistence.test.BaseDAOTestCase;
import com.rework.joss.persistence.test.biz.DAOTestDTO;
import com.rework.joss.persistence.test.biz.IMyTestDAO;

public class TplDAOTest extends BaseDAOTestCase {


	IMyTestDAO daoTestDAO;

	
	public void setDaoTestDAO(IMyTestDAO daoTestDAO) {
		this.daoTestDAO = daoTestDAO;
	}

	public void testTpl(){
		DAOTestDTO data = new DAOTestDTO();
		data.setTestint(new Integer(20));
		data.setTestname("TESTNAME1");
		data.setId("test_id_000");
		data.setTestId("ttest");
		daoTestDAO.create(data);
		
		List list = daoTestDAO.queryByTpl(
				"select *from dao_test where testname = '${name}'", 
				ConventionUtils.toMap(new String[]{"name", "TESTNAME1"}));
		list.size();;
		
	}
	
	public void testPrepareTpl(){
		DAOTestDTO data = new DAOTestDTO();
		data.setTestint(new Integer(20));
		data.setTestname("TESTNAME1");
		data.setId("test_id_000");
		data.setTestId("ttest");
		daoTestDAO.create(data);
		
		List list = daoTestDAO.queryByTpl(
				"select *from dao_test where testname = #${name}#", 
				ConventionUtils.toMap(new String[]{"name", "TESTNAME1"}));
		
		list.size();;
		
	}
	
	public void testTplWhere(){
		DAOTestDTO data = new DAOTestDTO();
		data.setTestint(new Integer(20));
		data.setTestname("TESTNAME1");
		data.setId("test_id_000");
		data.setTestId("ttest");
		daoTestDAO.create(data);
		
		List list = daoTestDAO.queryByTpl(
				"where testname = '${name}'", 
				ConventionUtils.toMap(new String[]{"name", "TESTNAME1"}));
		list.size();;
		
	}
	
	
	public void testTplInsert(){
		DAOTestDTO data = new DAOTestDTO();
		data.setTestint(new Integer(20));
		data.setTestname("TESTNAME1");
		data.setId("test_id_000");
		data.setTestId("ttest");
		daoTestDAO.create(data);
		data.setTestId("ttest1");
		daoTestDAO.create(data);
		data.setTestId("ttest2");
		daoTestDAO.create(data);
		data.setTestId("ttest3");
		daoTestDAO.create(data);
		
		
		
	}
}
