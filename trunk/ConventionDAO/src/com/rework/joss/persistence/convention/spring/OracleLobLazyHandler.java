/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.rework.joss.persistence.convention.spring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.OracleLobHandler;
import org.springframework.jdbc.support.nativejdbc.C3P0NativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.CommonsDbcpNativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.JBossNativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.SimpleNativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.WebLogicNativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.WebSphereNativeJdbcExtractor;

public class OracleLobLazyHandler extends OracleLobHandler{

	private static NativeJdbcExtractor extractor;
	
	public LobCreator getLobCreator() {
		return new OracleLobLazyCreator();
	}
	
	protected class OracleLobLazyCreator extends OracleLobCreator {
		private final Log logger = LogFactory.getLog(OracleLobLazyHandler.class);
		/**
		 * Retrieve the underlying OracleConnection, using a NativeJdbcExtractor if set.
		 */
		protected Connection getOracleConnection(PreparedStatement ps)
		throws SQLException, ClassNotFoundException {

			Connection con = ps.getConnection();
			if(isWebShpere(con)){
				logger.debug("使用WebSphere的数据源连接!");
				extractor = new WebSphereNativeJdbcExtractor();
			}else if(isWebLogic(con)){
				logger.debug("使用weblogic的数据源连接!");
				extractor =  new WebLogicNativeJdbcExtractor();
			}else if(isJboss(con)){
				logger.debug("使用jboss的数据源连接!");
				extractor = new JBossNativeJdbcExtractor();
			}else if(isTomcat55(con)){
				logger.debug("使用Tomcat5.5以上版本的DBCP数据源连接!");
				extractor = new CommonsDbcpNativeJdbcExtractor();
			}else if(isC3P0(con)) {
				logger.debug("使用C3P0数据源连接!");
				extractor = new C3P0NativeJdbcExtractor();
			}
			else{
				logger.debug("使用其它的数据源连接!");
				extractor = new SimpleNativeJdbcExtractor();
			}
			return extractor.getNativeConnectionFromStatement(ps);
		}
		private boolean isC3P0(Connection con){
			try{
				return getCurrnetClass("com.mchange.v2.c3p0.impl.NewProxyConnection").isAssignableFrom(con.getClass());
			}catch(ClassNotFoundException ex){
				logger.error("can not find class [com.mchange.v2.c3p0.impl.NewProxyConnection],maybe current server is not Weblogic",ex);
			}
			return false;
		}
		
		private boolean isWebLogic(Connection con){
			try{
				return getCurrnetClass("weblogic.jdbc.extensions.WLConnection").isAssignableFrom(con.getClass());
			}catch(ClassNotFoundException ex){
				logger.error("can not find class [weblogic.jdbc.extensions.WLConnection],maybe current server is not Weblogic",ex);
			}
			return false;
		}
		
		private boolean isWebShpere(Connection con){
			try{
				return getCurrnetClass("com.ibm.ws.rsadapter.jdbc.WSJdbcConnection").isAssignableFrom(con.getClass());
			}catch(ClassNotFoundException ex){
				logger.error("can not find class [com.ibm.ws.rsadapter.jdbc.WSJdbcConnection],maybe current server is not WebSphere",ex);
			}
			return false;
		}
		
		private boolean isJboss(Connection con){
			try{
				return getCurrnetClass("org.jboss.resource.adapter.jdbc.WrappedConnection").isAssignableFrom(con.getClass());
			}catch(ClassNotFoundException ex){
				logger.error("can not find class [org.jboss.resource.adapter.jdbc.WrappedConnection],maybe current server is not Joss",ex);
			}
			return false;
		}
		
		private boolean isTomcat55(Connection con){
			try{
				return getCurrnetClass("org.apache.tomcat.dbcp.dbcp.PoolingConnection").isAssignableFrom(con.getClass());
			}catch(ClassNotFoundException ex){
				logger.error("can not find class [org.apache.tomcat.dbcp.dbcp.PoolingConnection],maybe current server is not Tomcat 5.5 or above",ex);
			}
			return false;
		}
		
		private Class  getCurrnetClass(String test) throws ClassNotFoundException{
			return getClass().getClassLoader().loadClass(test);
		}

	}
}
