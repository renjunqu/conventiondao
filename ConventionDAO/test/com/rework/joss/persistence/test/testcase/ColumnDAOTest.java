package com.rework.joss.persistence.test.testcase;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.rework.joss.persistence.test.BaseDAOTestCase;
import com.rework.joss.persistence.test.biz.ColumnDAO;

public class ColumnDAOTest extends BaseDAOTestCase{
	@Autowired
	private ColumnDAO columnDAO;
	@Autowired
	private ColumnDAO columnDAO1;
	
	@Test
	public void testFindById() {
		List<Thread> threads = new ArrayList<Thread>();
		for(int i = 0; i < 40; i++) {
			threads.add(new Thread(new Runnable() {
				
				@Override
				public void run() {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					columnDAO.findById("xxx");
					columnDAO1.findById("fff");
				}
			}));
		}
		for (Thread t : threads) {
			t.start();
		}
		try {
			Thread.sleep(1000 * 60);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
