package com.rework.joss.persistence.test.testcase;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.rework.joss.persistence.test.BaseDAOTestCase;
import com.rework.joss.persistence.test.biz.AutoGenerateIdDAO;
import com.rework.joss.persistence.test.biz.AutoGenerateIdDAO2;
import com.rework.joss.persistence.test.biz.AutoGenerateIdDTO;

public class AutoGenerateIdDAOTest extends BaseDAOTestCase{
	@Autowired
	private AutoGenerateIdDAO autoGenerateIdDAO;
	@Autowired
	private AutoGenerateIdDAO2 autoGenerateIdDAO2;
	
	@Test
	public void testCreate() {
		for(int i = 0; i < 20; i++) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					for(int i = 0; i < 20; i++) {
						//测试多个表之间不会串
						AutoGenerateIdDTO dto = new AutoGenerateIdDTO();
						dto.setName(Thread.currentThread().getName() + "-xxx" + i);
						String id = autoGenerateIdDAO.createAndId(dto);
						org.junit.Assert.assertEquals(((AutoGenerateIdDTO)autoGenerateIdDAO.findById(id)).getName(), dto.getName());
						
						dto.setId(null);
						dto.setName(Thread.currentThread().getName() + "-2xxx" + i);
						id = autoGenerateIdDAO2.createAndId(dto);
						org.junit.Assert.assertEquals(((AutoGenerateIdDTO)autoGenerateIdDAO2.findById(id)).getName(), dto.getName());
					}
				}
			}).start();
		}
		try {
			Thread.sleep(20 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
