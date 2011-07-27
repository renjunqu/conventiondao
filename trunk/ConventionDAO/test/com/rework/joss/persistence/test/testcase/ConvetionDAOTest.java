/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.rework.joss.persistence.test.testcase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import com.rework.joss.persistence.IBaseDAO;
import com.rework.joss.persistence.test.BaseDAOTestCase;
import com.rework.joss.persistence.test.biz.GyXjStudentDTO;
import com.rework.utils.UUIDHexGenerator;

public class ConvetionDAOTest extends BaseDAOTestCase {
	IBaseDAO xgStudentDAO;
	
	IBaseDAO messageDAO;
	
	
	public IBaseDAO getMessageDAO() {
		return messageDAO;
	}

	public void setMessageDAO(IBaseDAO messageDAO) {
		this.messageDAO = messageDAO;
	}

	public void setXgStudentDAO(IBaseDAO xgStudentDAO) {
		this.xgStudentDAO = xgStudentDAO;
	}
	

	
	public GyXjStudentDTO getTestStudentDTO(){
		GyXjStudentDTO student = new GyXjStudentDTO();
		student.setXb("锟斤拷");
		student.setXm("test");
		student.setXh("1111");
		student.setXz("4");
		student.setNj("2005");
		student.setPycc("锟斤拷锟斤拷");
		return student;
	}
	public void testInsert(){
		GyXjStudentDTO student = getTestStudentDTO();
		student.setXh("2005013308");
		xgStudentDAO.create(student);
	}
	
	public void testInserts(){
		GyXjStudentDTO student = getTestStudentDTO();
		student.setXh("2005013308");
		
		GyXjStudentDTO student2 = getTestStudentDTO();
		student2.setXh("2005013308");
		
		GyXjStudentDTO student3 = getTestStudentDTO();
		student3.setXh("2005013308");
		
		xgStudentDAO.create(new GyXjStudentDTO[]{student, student3, student2});
	}
	
	public void testFindById(){
		GyXjStudentDTO student = getTestStudentDTO();
		student.setXh("2005013308");
		xgStudentDAO.create(student, "0");
		GyXjStudentDTO student2 = (GyXjStudentDTO)xgStudentDAO.findById(student.getStudentId());
		assertNotNull(student2);
	}
	
	public void testFindByIds(){
		
		GyXjStudentDTO student = getTestStudentDTO();
		student.setXh("2005013308");
		xgStudentDAO.create(student, "123124234sdf");
		List list = xgStudentDAO.findByIds(new Object[]{student.getStudentId()});
		
		Assert.assertEquals(list.size(), 1);
	}
	
	public void testInsertWithId(){
		GyXjStudentDTO student = getTestStudentDTO();
		student.setStudentId(student.getXh());
		xgStudentDAO.create(student);
	}
	
	public void testUpdate(){
		GyXjStudentDTO student = getTestStudentDTO();
		student.setXh("2005013308");
		xgStudentDAO.createAndId(student);
		
		student.setXh("2005013309");
		xgStudentDAO.update(student);
	}
	
	public void testUpdateByMap(){
		GyXjStudentDTO student = getTestStudentDTO();
		student.setXh("2005013308");
		xgStudentDAO.createAndId(student);
		
		Map map = new HashMap();
		map.put("xb", "男");
		map.put("xm", "我是欧元");
		map.put("xh", "00000000");
		map.put("studentId", student.getStudentId());
		xgStudentDAO.update(map);
	}
	
	public void testRemoveById(){
		xgStudentDAO.remove("111");
	}
	
	public void testQuery(){
		GyXjStudentDTO query = new GyXjStudentDTO();
		query.setXh("111");
		query.setXm("test");
		xgStudentDAO.query(query, 0,10,"xh");
	}
	
	public void testQueryAll(){
		GyXjStudentDTO query = new GyXjStudentDTO();
		query.setXh("111");
		query.setXm("test");
		xgStudentDAO.query(query,"xh");
	}
	public void testQueryCount(){
		xgStudentDAO.queryCount(new GyXjStudentDTO());
	}
	public void testQueryCountByCriteria(){
		xgStudentDAO.queryCount("xh='333'");
	}
	public void testQueryCountByDtoAndCriteria(){
		GyXjStudentDTO query = new GyXjStudentDTO();
		query.setXb("锟斤拷");
		xgStudentDAO.queryCount(query, "pycc in('锟斤拷锟斤拷','专锟斤拷')");
	}
	
	
	public void testRemoveByCondition(){
		GyXjStudentDTO query = new GyXjStudentDTO();
		query.setXh("xh");
		query.setXm("xm");
		xgStudentDAO.removeByCondition(query);
		Map paramMap = new HashMap();
		paramMap.put("id", "111");
		paramMap.put("xh", "22");
		xgStudentDAO.removeByCondition(paramMap);
	}
	public void testUpdateByConditionIgnoreEmpty(){
		GyXjStudentDTO data = new GyXjStudentDTO();
		data.setXh("xh");
		data.setXm("xm");
		data.setBh("bh");
		data.setBjmc("");
		GyXjStudentDTO condition = new GyXjStudentDTO();
		condition.setStudentId("id1");
		condition.setBh("bh1");
		condition.setXy("xy1");
		xgStudentDAO.updateByConditionIgnoreEmpty(data, condition);
		condition.setStudentId("id2");
		condition.setBh("bh2");
		condition.setXy("xy2");
		xgStudentDAO.updateByCondtion(data,condition);
	}
	
	public void testQueryByCriteria(){
		xgStudentDAO.queryByCriteria("and xh='111' order by xh");
	}
	
	public void testQueryByCriteriaHasAnd(){
		xgStudentDAO.queryByCriteria(" and xh='111' order by xh");
	}
	
	public void testRemoveByCriteria(){
		xgStudentDAO.removeByCriteria("xh='ddd'");
		
	}
	@SuppressWarnings("unchecked")
	public void testQueryNestObject(){
		List<GyXjStudentDTO> list = xgStudentDAO.queryByTpl("select xh as \"message.title\" from gy_xj_student");
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			GyXjStudentDTO gyXjStudentDTO = (GyXjStudentDTO) iterator.next();
			gyXjStudentDTO.getBh();
		}
	}
	
	
}
