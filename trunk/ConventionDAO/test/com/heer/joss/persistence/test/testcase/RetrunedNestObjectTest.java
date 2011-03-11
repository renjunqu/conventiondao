/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.heer.joss.persistence.test.testcase;

import java.util.Iterator;
import java.util.List;

import com.heer.joss.persistence.test.BaseDAOTestCase;
import com.heer.joss.persistence.test.biz.GyXjStudentDTO;
import com.rework.joss.persistence.IBaseDAO;

public class RetrunedNestObjectTest extends BaseDAOTestCase {
	IBaseDAO xgStudentDAO;
	
	
	@SuppressWarnings("unchecked")
	public void testQueryNestObject(){
		List<GyXjStudentDTO> list = xgStudentDAO.queryByTpl(
				"select xh as \"message.title\" from gy_xj_student");
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			GyXjStudentDTO gyXjStudentDTO = (GyXjStudentDTO) iterator.next();
			gyXjStudentDTO.getBh();
		}
	}
	
	
}
