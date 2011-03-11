/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.rework.joss.persistence.convention.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.rework.joss.persistence.convention.ConventionUtils;
import com.rework.joss.persistence.convention.db.model.ColumnBean;
import com.rework.joss.persistence.convention.db.model.Container;
import com.rework.joss.persistence.convention.db.model.TableBean;

public class PreLoadContainerOracleFactory implements FactoryBean {

	private static Log logger = LogFactory.getLog(PreLoadContainerOracleFactory.class);
	
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

	
	
	/**
	 * 
	 * @param container
	 * @param tableCondition eg. col.TABLE_NAME like 'JW%'
	 */
	public void preLoadTableColumns(final Container container) {
		
		String preLoadSql = 
			 "select com.column_name,                                                       "
			+"       com.comments,                                                          "
			+"       col.TABLE_NAME,                                                        "
			+"       col.DATA_TYPE,                                                         "
			+"       col.NULLABLE,                                                          "
			+"       col.CHAR_LENGTH,                                                       "
			+"       col.DATA_SCALE,                                                        "
			+"       a.constraint_type,                                                     "
			+"       o.object_type                                                          "
			+"  from user_tab_columns col                                                   "
			+" inner join user_col_comments com on col.COLUMN_NAME = com.column_name        "
			+"                                 and col.TABLE_NAME = com.table_name          "
			+"  left join user_objects o on o.object_name = col.TABLE_NAME                  "
			+"  left join (select col.table_name,col.column_name, cs.constraint_type                                "
			+"           from user_constraints cs                                                    "   
			+"           join user_cons_columns col on cs.constraint_name = col.constraint_name      "      
			+"           where cs.constraint_type = 'P'                                              "
			+ "and (" +preLoadCondition +")) a on col.COLUMN_NAME = a.column_name  and col.table_name = a.table_name      " 
			+" where                                                                        "
			
			+ "(" +preLoadCondition +")"
			
			+"   and (o.object_type = 'TABLE' or o.object_type = 'VIEW')                    "
			+" order by table_name                                                          ";
		
		JdbcTemplate jdt = new JdbcTemplate(dataSource);
		
		try {
			jdt.query(preLoadSql, new RowCallbackHandler(){
				
				public void processRow(ResultSet columns) throws SQLException  {
					String tableName = columns.getString("table_name");
					TableBean dbTable = getTableFromContainer(container, tableName);
					String columnName = columns.getString("COLUMN_NAME");
					String datatype = columns.getString("data_type");
					int datasize = columns.getInt("char_length");
					int digits = columns.getInt("data_scale");
					int nullable = columns.getString("NULLABLE").equals("Y")?1:0;

					int jdbcType = ConventionUtils.getJdbcType(datatype);
					
					String remark = columns.getString("comments");
					String constranintType = columns.getString("constraint_type");
					String objectType = columns.getString("object_type");
					dbTable.setObjectType(objectType);
					ColumnBean newColumn = new ColumnBean(dbTable, columnName, datatype,
							jdbcType, datasize, digits, nullable, remark);
					dbTable.notifyColumn(newColumn);
					if("P".equals(constranintType)){
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
