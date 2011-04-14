﻿/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.heer.joss.persistence.test.testcase;

import com.heer.joss.persistence.test.BaseDAOTestCase;
import com.heer.joss.persistence.test.biz.DAOTestDTO;
import com.rework.joss.persistence.IBaseDAO;

public class BooleanDAOTest extends BaseDAOTestCase {
	
	
	
	IBaseDAO booleanDAO;
	


	public void setBooleanDAO(IBaseDAO booleanDAO) {
		this.booleanDAO = booleanDAO;
	}



	public void testBoolean() {
		DAOTestDTO newdto = new DAOTestDTO();
		newdto.setTestboolean(false);
		
		String id = booleanDAO.createAndId(newdto);
		
		DAOTestDTO dto = (DAOTestDTO)booleanDAO.findById(id);
		
		assertEquals(Boolean.FALSE, dto.getTestboolean());
	}

}