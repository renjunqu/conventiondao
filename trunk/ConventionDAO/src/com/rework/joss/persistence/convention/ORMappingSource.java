/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.rework.joss.persistence.convention;

import javax.sql.DataSource;

import org.springframework.util.Assert;

import com.rework.joss.persistence.convention.db.DBFactory;
import com.rework.joss.persistence.convention.db.IDBFacade;
import com.rework.joss.persistence.convention.db.model.Container;
import com.rework.joss.persistence.convention.db.model.TableBean;

/**
 * or mapping 信息的源
 * <p>
 * 全部缓存在这里
 * 在第一次获取时初始化到缓存中
 * 
 * @author zhangsh
 *
 */
public class ORMappingSource {
	
	private static Container container;

	private DataSource dataSource;
	
	public void setContainer(Container container) {
		ORMappingSource.container = container;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public DataSource getDataSource() {
		return dataSource;
	}

	private TableBean getMetaNotFromCache( String dbo){
		// 从数据库的metadata中取出字段信息集合
		Assert.notNull(dbo);
		IDBFacade db = DBFactory.getDBFacade(this.dataSource);
		try{
			db.populateTableData(container,dbo);
		}catch(Exception ex){
			//读取表结构够失败
			ex.printStackTrace();
		}
		TableBean table = container.getTable(dbo);
		return table;
	}
	
	public boolean isTableExist(String tablename){
		if( null != container && container.hasTable(tablename) ){
			return true;
		} 
		return false;
	}
	
	public TableBean getTableMetaData( String dbo) {
		// 如果没有被自动注入,说明没有定义预加载container
		if(null == container){
			container = new Container();
		}
		if(container.hasTable(dbo)){
			TableBean dboInCache = (TableBean) container.getTable(dbo);
			return (TableBean)dboInCache.clone();
		}else{
			return getMetaNotFromCache( dbo);
		}
	}
	
	/**
	 * 重新从数据库读取表结构
	 */
	public TableBean freshTableMetaData(String dbo) {
		if(null == container){
			container = new Container();
		}
		return getMetaNotFromCache( dbo);
	}
}