/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.rework.joss.persistence.convention;


/**
 * 基于heer数据库规范建立的转换策略
 * @author zhangsh
 *
 */
public class DefaultConventionStrategy implements IConventionStrategy {

	public String translateFromColumnToProperty(String columnNam, boolean isPrimaryKey) {
		return ConventionUtils.translateFromColumnToProperty(columnNam);
	}
	
	/**
	 * 数据库中是 表名 去掉 两位系统前缀_+_ID 
	 */
	public String getLogicIdName(String table) {
		return table.substring(3) + "_ID";
	}
	
	public String translateFromPropertyToColumn(String propertyName){
		return ConventionUtils.translateFromPropertyToColumn(propertyName);
	}
}
