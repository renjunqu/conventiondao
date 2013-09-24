/*
 * Copyright 2004-2005 HeerIT Team No.1@ZhuHai
 * Project Name : ctbuxg
 * package com.rework.zsu.xg;.XgTestCase.java
 */

package com.rework.joss.persistence.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;



/**
 * DAO测试的基类
 * <p>
 * TODO 还需要mock一些常用的数据库记录进行辅助，例如,student ,user .....
 * 
 * @since 2005-3-30
 * @author zhangsh
 * @version $Id: BaseDAOTestCase.java,v 1.1 2009/07/24 04:33:14 zhujj Exp $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:applicationSourceTest.xml", "classpath:com/rework/joss/persistence/test/daoContext.xml"})
@TransactionConfiguration(transactionManager="transactionManager", defaultRollback=true)
public class BaseDAOTestCase{
	protected Log logger = LogFactory.getLog(getClass());

	static {
		// mock a datasource into the jndi context
		LocalJndiDataSource.mock();
	}

	public static void assertConcurrent(final String message, final List<? extends Runnable> runnables, final int maxTimeoutSeconds) throws InterruptedException {
		final int numThreads = runnables.size();
		final List<Throwable> exceptions = Collections.synchronizedList(new ArrayList<Throwable>());
		final ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);
		try {
			final CountDownLatch allExecutorThreadsReady = new CountDownLatch(numThreads);
			final CountDownLatch afterInitBlocker = new CountDownLatch(1);
			final CountDownLatch allDone = new CountDownLatch(numThreads);
			for (final Runnable submittedTestRunnable : runnables) {
				threadPool.submit(new Runnable() {
					public void run() {
						allExecutorThreadsReady.countDown();
						try {
							afterInitBlocker.await();
							submittedTestRunnable.run();
						} catch (final Throwable e) {
							exceptions.add(e);
						} finally {
							allDone.countDown();
						}
					}
				});
			}
			// wait until all threads are ready
			assertTrue("Timeout initializing threads! Perform long lasting initializations before passing runnables to assertConcurrent", allExecutorThreadsReady.await(runnables.size() * 10, TimeUnit.MILLISECONDS));
			// start all test runners
			afterInitBlocker.countDown();
			assertTrue(message +" timeout! More than" + maxTimeoutSeconds + "seconds", allDone.await(maxTimeoutSeconds, TimeUnit.SECONDS));
		} finally {
			threadPool.shutdownNow();
		}
		assertTrue(message + "failed with exception(s)" + exceptions, exceptions.isEmpty());
	}
}
