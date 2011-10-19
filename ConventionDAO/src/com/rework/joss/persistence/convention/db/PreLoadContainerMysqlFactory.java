/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.rework.joss.persistence.convention.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.rework.joss.persistence.convention.ConventionUtils;
import com.rework.joss.persistence.convention.db.model.ColumnBean;
import com.rework.joss.persistence.convention.db.model.Container;
import com.rework.joss.persistence.convention.db.model.TableBean;

public class PreLoadContainerMysqlFactory implements FactoryBean {

	private static Log logger = LogFactory.getLog(PreLoadContainerMysqlFactory.class);
	
	private String preLoadCondition;
	
	private DataSource dataSource;
	
	public void setPreLoadCondition(String preLoadCondition) {
		this.preLoadCondition = preLoadCondition;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Object getObject() throws Exception {
		Container container = new Container();
		preLoadTableColumns(container);
		return container;
	}

	public Class getObjectType() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSingleton() {
		return true;
	}

	private String getSchemaName(){
		
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			return conn.getCatalog();
		} catch (SQLException e) {
			throw new DataAccessException(e);
		} finally {
			DataSourceUtils.releaseConnection(conn, dataSource);
		}
	}
	
	/**
	 * 
	 * @param container
	 * @param tableCondition eg. col.TABLE_NAME like 'JW%'
	 */
	public void preLoadTableColumns(final Container container) {
		
		
		String preLoadSql = 
		"select " +
		"c.column_name,c.column_comment,c.table_name,c.data_type,c.is_nullable,c.character_maximum_length ,c.numeric_scale ,c.column_key,t.table_type "+ 
		"from information_schema.columns c, information_schema.tables t "+
		"where c.table_name = t.table_name "+
		"and (t.table_type = 'BASE TABLE' or t.table_type = 'VIEW') " +
		"and c.table_schema = '"+ getSchemaName() +"' " +
		"and t.table_schema = '"+ getSchemaName() +"' " +
		"group by "+
		"c.column_name,c.column_comment,c.table_name,c.data_type,c.is_nullable,c.character_maximum_length ,c.numeric_scale ,c.column_key,t.table_type "
		;
		
		logger.debug("begin to init db objects for convention dao!");
		
		JdbcTemplate jdt = new JdbcTemplate(dataSource);
		
		try {
			jdt.query(preLoadSql, new RowCallbackHandler(){
				
				public void processRow(ResultSet columns) throws SQLException  {
					String tableName = columns.getString("table_name").toUpperCase();
					TableBean dbTable = getTableFromContainer(container, tableName);
					String columnName = columns.getString("COLUMN_NAME");
					String datatype = columns.getString("data_type");
					long datasize = columns.getLong("character_maximum_length");
					int digits = columns.getInt("numeric_scale");
					int nullable = columns.getString("is_nullable").equals("YES")?1:0;

					int jdbcType = ConventionUtils.getJdbcType(datatype);
					
					String remark = columns.getString("column_comment");
					String constranintType = columns.getString("column_key");
					String objectType = columns.getString("table_type");
					if( "BASE TABLE".equals(objectType) ){
						objectType = "TABLE";
					}
					dbTable.setObjectType(objectType);
					ColumnBean newColumn = new ColumnBean(dbTable, columnName, datatype,
							jdbcType, datasize, digits, nullable, remark, false);
					dbTable.notifyColumn(newColumn);
					if("PRI".equals(constranintType)){
						dbTable.notifyPrimaryKey(checkName(columnName));
					}
				}


			});
		} catch (RuntimeException e) {
			logger.error("ConventionDAO　预加载失败!", e);
		}

	}


	private String checkName(String s) {
		if (null == s)
			return null;
		s = ConventionUtils.stringReplace(s, "`", "");
		return s;
	}
	private TableBean getTableFromContainer(final Container container,
			String tableName) {
		TableBean dbTable = container.getTable(tableName);
		if(dbTable == null){
			dbTable = new TableBean(tableName);
			container.addTable(tableName, dbTable);
		}
		return dbTable;
	}	

	
	
	
	
	
}
