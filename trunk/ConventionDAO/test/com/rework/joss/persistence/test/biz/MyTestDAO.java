/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */ 
package com.rework.joss.persistence.test.biz;

import java.util.HashMap;
import java.util.List;

import com.rework.joss.persistence.convention.BaseDAOByConvention;

public class MyTestDAO extends BaseDAOByConvention implements IMyTestDAO{
	
	public List getTestByTestname(String testname){
		HashMap tempMap = new HashMap();
		tempMap.put("name1",testname);
		tempMap.put("int1",	"20");
		return queryForListByMap("getTestByTestname",tempMap);
	}
	
	public List queryDAOTest(DAOTestDTO query){
		return queryForListByBaseObject("queryDAOTest", query);
	}
	
	public DAOTestDTO getTestByTestId(String testId){
		DAOTestDTO test = new DAOTestDTO();
		test.setId(testId);
		return (DAOTestDTO)queryForBaseObject("getTestByTestId", test);
	}
	
	
	public int getMaxTestInt(){
		return ((Integer)queryForObject("getMaxTestInt", new HashMap(), Integer.class)).intValue();
	}
}
