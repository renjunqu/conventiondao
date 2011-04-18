package com.rework.joss.persistence.test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.NamingManager;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * <p>在本地环境下使用<code>JNDI</code>的数据源, 主要目的是为了不修改代码或配置信息而进行测试.
 * 数据源信息可以用两种方式指定<ol>
 * <li>在根类路径下面创建<code>jdbc.properties</code>文件, 文件中每一项定义了一个数据库
 * 连接属性, <code>key</code>与<code>BasicDataSource</code>含义相同, 比如:
 * <pre>
 * <code>driverClassName=oracle.jdbc.driver.OracleDriver</code>
 * <code>url=jdbc:oracle:thin:@192.168.0.2:1521:heer</code>
 * </pre></li>
 * <li>调用带参数的<code>mock</code>方法, 数据库连接参数作为<code>Properties</code>指定</li>
 * </ol></p>
 * <p>在使用任何<code>jndi datasource</code>的操作之前调用这个类的<code>mock</code>方法,
 * 按照第二种使用方法程序的写法如下:
 * <pre>
 *   Properties p = new Properties();
 *   p.put("driverClassName", "oracle.jdbc.driver.OracleDriver");
 *   p.put("url", "jdbc:oracle:thin:@192.168.0.2:1521:heer");
 *   p.put("username", "zsu_dev");
 *   p.put("password", "zsuok");
 *   ......
 *   
 *   // 在使用JDNI数据源之前做这个调用
 *   LocalJndiDataSource.mock(p);
 *   ......
 *   
 * </pre>
 * </p>
 * <p>
 * 
 * 使用{@link org.springframework.jdbc.datasource.SingleConnectionDataSource}以减少启动时间
 * 
 * @author zhangl
 * @author zhangsh
 * @since 2005-11-8
 * @version $Id: LocalJndiDataSource.java,v 1.1 2009/07/24 04:33:14 zhujj Exp $
 */
public class LocalJndiDataSource implements InitialContextFactory, InitialContextFactoryBuilder, InvocationHandler {
    private static final Log logger = LogFactory.getLog(LocalJndiDataSource.class);
    private static LocalJndiDataSource instance;
    private static Hashtable boundObjects = new Hashtable(3);
    
    private static DriverManagerDataSource dataSource;
    
    public static final String DRIVER_CLASS_NAME_KEY = "driverClassName";
    public static final String URL_KEY = "url";
    public static final String USER_NAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String JNDI_NAME = "jdbc/oracleDS";
    public static final String ENV_NAME = "java:comp/env";
    public static final String JDBC_PROPERTIES = "jdbc.properties";

    private LocalJndiDataSource() {
        try {
            NamingManager.setInitialContextFactoryBuilder(this);
        } catch (NamingException e) {
            logger.error("设置本地JNDI工厂错误," + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
    /**
     * 开始设置<code>JNDI</code>执行环境, 这个方法在一个<code>JVM</code>内部只能执行一
     * 次, 数据库的连接配置写在根类路径下面, 名称是<code>jndi.properties</code>
     */
    public synchronized static void mock() {
        Properties p = new Properties();
        try {
            InputStream in = Thread
                .currentThread()
                .getContextClassLoader()
                .getResourceAsStream(JDBC_PROPERTIES);
                //LocalJndiDataSource.class.getResourceAsStream("jndi.properties");
            if (in == null) {
                throw new IOException("没有在根路径下找到数据库连接参数文件" + JDBC_PROPERTIES);
            }
            
            p.load(in);
            mock(p);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
    /**
     * 开始设置<code>JNDI</code>执行环境, 这个方法在一个<code>JVM</code>内部只能执行一
     * 次, 数据库的连接配置通过参数设置, 参数中<code>key</code>的名称见这个类的常量, 这是
     * <code>BasicDataSource</code>的属性命名
     */
    public synchronized static void mock(Properties p) {
        if (instance == null) {
            instance = new LocalJndiDataSource();
            
            dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName(p.getProperty(DRIVER_CLASS_NAME_KEY));
            dataSource.setUrl(p.getProperty(URL_KEY));
            dataSource.setUsername(p.getProperty(USER_NAME_KEY));
            dataSource.setPassword(p.getProperty(PASSWORD_KEY));
            boundObjects.put(JNDI_NAME, dataSource);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("不应该再调用这个方法", new IllegalStateException());
            }
            //throw new IllegalStateException("不应该再调用这个方法");
        }
    }

    public InitialContextFactory createInitialContextFactory(Hashtable environment) throws NamingException {
        return this;
    }

    public Context getInitialContext(Hashtable environment) throws NamingException {
        return (Context) Proxy.newProxyInstance(
            getClass().getClassLoader(), new Class[] {Context.class}, this);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("lookup".equals(method.getName()) && args.length == 1 && args[0] instanceof String) {
            String name = (String) args[0];
            if (ENV_NAME.equals(name) || (ENV_NAME + "/").equals(name)) {
                return getInitialContext(null);
            } else if (name.startsWith(ENV_NAME)) {
                name = name.substring(ENV_NAME.length() + 1);
            }

            return boundObjects.get(name); 
        }
        
        logger.warn("calling the invalid method[" + method + "]");
        
        return null;
    }
    
    /**
     * 得到测试数据源
     * @return
     */
    public static DataSource getDataSource(){
    	if(null == dataSource){
    		mock();
    	}
    	return dataSource;
    }
}
