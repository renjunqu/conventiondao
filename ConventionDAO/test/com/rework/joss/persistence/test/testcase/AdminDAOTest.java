package com.rework.joss.persistence.test.testcase;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.rework.joss.persistence.convention.ORMappingSource;
import com.rework.joss.persistence.test.BaseDAOTestCase;
import com.rework.joss.persistence.test.biz.AdminDAO;
import com.rework.joss.persistence.test.biz.AdminDTO;

/**
 * 主要测试懒式加载是否可用
 * @author zhujj
 *
 */
public class AdminDAOTest extends BaseDAOTestCase{
	private static int threadIndex = 1;
	
	@Autowired
	private AdminDAO adminDAO;
	@Resource(name="metaSource")
	private ORMappingSource mappingSource;
	
	@Test
	public void testInsert() {
		Assert.assertEquals(mappingSource.isTableExist("admin"), false);
		
		AdminDTO admin = new AdminDTO();
		admin.setUsername("test");
		admin.setPassword("password");
		adminDAO.create(admin);
		
		admin.setUsername("test1");
		adminDAO.create(admin);
		
		Assert.assertEquals(mappingSource.isTableExist("admin"), true);
	}
	
	@Test
	public void testInsertMuch() {
		for(int i = 0; i < 1000; i++) {
			adminDAO.create(new AdminDTO("test" + i, "test" + i));
		}
	}
	
	public synchronized int getThreadIndex() {
		return threadIndex ++;
	}
	
	@Test
	public void testMultiCreate() throws InterruptedException {
		
		List<Runnable> threadList = new ArrayList<Runnable>();
		for(int i = 0; i < 10; i++) {
			Runnable thread = new Runnable() {
				@Override
				public void run() {
					AdminDTO admin = new AdminDTO();
					int index = getThreadIndex();
					admin.setUsername("test" + index);
					admin.setPassword("test" + index);
					adminDAO.create(admin);
				}
			};
			threadList.add(thread);
		}
		assertConcurrent("完成", threadList, 100000);
	}
}
