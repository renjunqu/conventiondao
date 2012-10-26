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
 * java属性和数据库字段的转换策略,
 * 
 * <p>
 * 这个策略是这个模块的核心,正是由于策略的存在,才使得我们可以简化o/r mapping的配置
 * @author zhangsh
 *
 */
public interface IConventionStrategy {

	public String translateFromColumnToProperty(String columnName, boolean isPrimaryKey);
	
	public String getLogicIdName(String table);
	
	/**
	 * 将属性名向字段名转换
	 * @param propertyName
	 * @return
	 */
	public String translateFromPropertyToColumn(String propertyName);
}
