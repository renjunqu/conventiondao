package com.heer.joss.persistence.test.biz;

import com.heer.joss.persistence.test.BaseDAOTestCase;
import com.rework.joss.persistence.IBaseDAO;

/**
 * 测试视图相关方法
 * 
 * @author zhujj
 * @create 2008-3-26
 */
public class ConventionDAO4ViewTest extends BaseDAOTestCase {
	private IBaseDAO vwGyUserDAO;

	public void setVwGyUserDAO(IBaseDAO vwGyUserDAO) {
		this.vwGyUserDAO = vwGyUserDAO;
	}
	
	public void testFindById(){
		VwGyUserDTO result = (VwGyUserDTO)vwGyUserDAO.findById("test");
		assertNull(result);
	}
	public void testIdRetrive(){
		VwGyUserDTO result = (VwGyUserDTO)vwGyUserDAO.findById("test");
		assertNull(result.getId());
	}
}
