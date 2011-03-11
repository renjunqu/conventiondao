/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.heer.joss.persistence.test.testcase;

import com.heer.joss.persistence.test.BaseDAOTestCase;
import com.heer.joss.persistence.test.biz.MessageDTO;
import com.rework.joss.persistence.IBaseDAO;

public class OracleClobDAOTest extends BaseDAOTestCase {
	
	IBaseDAO messageDAO;
	
	
	public IBaseDAO getMessageDAO() {
		return messageDAO;
	}

	public void setMessageDAO(IBaseDAO messageDAO) {
		this.messageDAO = messageDAO;
	}


	private static String oldMsg;
	public void testClobRead() {
		MessageDTO dto = (MessageDTO)messageDAO.findById("eval_report_admin");
		assertNotNull(dto.getMessage());
		oldMsg = dto.getMessage();
	}
	public void testClobWrite() {
		MessageDTO dto = new MessageDTO();
		dto.setId("eval_report_admin");
		dto.setMessage(oldMsg);
		messageDAO.updateIgnoreEmpty(dto);
		assertEquals(dto.getMessage(), oldMsg);
	}
}
