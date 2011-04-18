/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.rework.joss.persistence.test.testcase;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import com.rework.joss.persistence.IBaseDAO;
import com.rework.joss.persistence.convention.ConventionUtils;
import com.rework.joss.persistence.test.BaseDAOTestCase;
import com.rework.joss.persistence.test.biz.DAOTestDTO;
import com.rework.joss.persistence.test.biz.IMyTestDAO;


public class FeatureDAOTest extends BaseDAOTestCase {
	IMyTestDAO daoTestDAO;

	IBaseDAO testDAO4ColMap;
	
	public void setDaoTestDAO(IMyTestDAO daoTestDAO) {
		this.daoTestDAO = daoTestDAO;
	}
	
	public void setTestDAO4ColMap(IBaseDAO testDAO4ColMap) {
		this.testDAO4ColMap = testDAO4ColMap;
	}

	public void testDAO4ColMap(){
		DAOTestDTO data = new DAOTestDTO();
		data.setTestint(new Integer(20));
		data.setTestname("TESTNAME1");
		data.setId("test_id_000");
		data.setTestId("ttest");
		data.setNotMatchId("test_notmatch_id_000");
		testDAO4ColMap.create(data);
		DAOTestDTO testDTO = (DAOTestDTO) testDAO4ColMap.findById(data.getNotMatchId());
		assertNotNull(testDTO);
	}

	public void testGetTestByTestname(){
		List list  = daoTestDAO.getTestByTestname("TESTNAME1");
		assertEquals(list.size(), 1);
		daoTestDAO.getTestByTestname("我的天空");
	}
	public void testGetTestByTestId(){
		DAOTestDTO dto = daoTestDAO.getTestByTestId("TEST_ID1");
		assertNotNull(dto);
	}
	public void testGetMaxTestId(){
		int testInt = daoTestDAO.getMaxTestInt();
		assertEquals(testInt, 20);
	}
	
	public void testqueryForListByBaseObject(){
		DAOTestDTO data = new DAOTestDTO();
		data.setTestint(new Integer(20));
		data.setTestname("TESTNAME1");
		List resultList = daoTestDAO.queryDAOTest(data);
		assertEquals(resultList.size(), 1);
		
		ArrayList testlist = new ArrayList();
		data.setTestlist(testlist);
		daoTestDAO.queryDAOTest(data);
		
		testlist.add("1111111111111");
		testlist.add("2222222222222");
		data.setTestlist(testlist);
		daoTestDAO.queryDAOTest(data);
		
		testlist.add(null);
		testlist.add("");
		testlist.add("333333333333");
		daoTestDAO.queryDAOTest(data);
	}
	
	public void testgetTestDate(){
		daoTestDAO.queryForObject("getTestDate", ConventionUtils.toMap("id", "ID1"), Date.class);
	}
	
	public void testexecuteUpdate(){
		daoTestDAO.executeUpdate("updateDaoTest", ConventionUtils.toMap("testname", "haha"));
		daoTestDAO.executeUpdate("insertDaoTest", null);
	}
}
