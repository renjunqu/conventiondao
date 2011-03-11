/*
 * Copyright 2004-2005 HeerIT Team No.1@ZhuHai
 * Project Name : ctbuxg
 * package com.rework.zsu.xg;.XgTestCase.java
 */

package com.heer.joss.persistence.test;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.test.AbstractTransactionalSpringContextTests;



/**
 * DAO测试的基类
 * <p>
 * TODO 还需要mock一些常用的数据库记录进行辅助，例如,student ,user .....
 * 
 * @since 2005-3-30
 * @author zhangsh
 * @version $Id: BaseDAOTestCase.java,v 1.1 2009/07/24 04:33:14 zhujj Exp $
 */
public class BaseDAOTestCase extends AbstractTransactionalSpringContextTests {

	static {
		// mock a datasource into the jndi context
		LocalJndiDataSource.mock();
	}
	
	public BaseDAOTestCase(){
		super();
		super.setAutowireMode(AutowireCapableBeanFactory.AUTOWIRE_BY_NAME);
	}

	protected String[] getConfigLocations() {
		
		return new String[]{
				// 对象依赖关系 从上到下，上面配置文件中的bean不能依赖下面的
				"applicationSourceTest.xml",
				"classpath:com/heer/joss/persistence/test/daoContext.xml"
		};
	}


}
