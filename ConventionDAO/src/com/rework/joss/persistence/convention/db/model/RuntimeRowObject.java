/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.rework.joss.persistence.convention.db.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rework.core.dto.BaseObject;
import com.rework.joss.persistence.convention.ConventionUtils;
import com.rework.joss.persistence.convention.IConventionStrategy;

/**
 * 分析sqlmap的时候用到的对象,
 * 这是一个运行时的对象,
 * 
 * <li>数据库对象结构 dbo
 * <li>结果列 columns
 * <li>查询列 conditions
 * <li>pojo对象
 * 
 * <code>
 * 	select columns from dbo where conditions = rowObj
 * </code>
 * 
 * @author zhangsh
 * 
 */
public class RuntimeRowObject {

	private Log logger = LogFactory.getLog(RuntimeRowObject.class);

	/**
	 * 转换策略
	 */
	private IConventionStrategy conventionStrategy;

	/**
	 * 不能被修改,这应该是一个缓存的对象
	 */
	private TableBean dbo;

	/**
	 * 值对象
	 */
	private Object dataObj;

	/**
	 * 具有不为空的操作结果字段列表
	 */
	private List columnsWithNotEmptyValue = new ArrayList();
	/**
	 * 具有不为null的结果字段列表
	 */
	private List columnsWithNotNullValue = new ArrayList();
	/**
	 * 具有不忽略null的结果字段列表
	 */
	private List columnsWithNotIgnoreNull = new ArrayList();
	/**
	 * 具有不为空的条件字段列表, 当更新的列和用于查询的列不一样的情况
	 */
	private List conditionsWithNotEmptyValue = new ArrayList();
	private List conditionsWithNotNullValue = new ArrayList();
	private List conditionsWithNotIgnoreNullValue = new ArrayList();

	public List getConditionsWithNotIgnoreNullValue() {
		return conditionsWithNotIgnoreNullValue;
	}

	public void setConditionsWithNotIgnoreNullValue(
			List conditionsWithNotIgnoreNullValue) {
		this.conditionsWithNotIgnoreNullValue = conditionsWithNotIgnoreNullValue;
	}

	/**
	 * 包装值对象的值进来
	 * 
	 * @param obj
	 */
	public RuntimeRowObject(TableBean dbo, Object rowObj, Object conditionObj,
			IConventionStrategy conventionStrategy) {
		// a copy for dbo
		this.dbo = (TableBean) dbo.clone();
		this.dataObj = rowObj;
		this.conventionStrategy = conventionStrategy;
		
		List columnsWithNotNullValue = new ArrayList();
		List columnsWithNotEmptyValue = new ArrayList();
		List columnsWithNotIgnoreNull = new ArrayList();
		List conditionsWithNotNullValue = new ArrayList();
		List conditionsWithNotEmptyValue = new ArrayList();
		List conditionsWithNotIgnoreNullValue = new ArrayList();
		for (Iterator iterator = dbo.getColumns().iterator(); iterator.hasNext();) {
			Object propValue = null;
			ColumnBean column = (ColumnBean) iterator.next();
			// 不应该对缓存的 TableBean 进行修改
			ColumnBean conditionColumn = (ColumnBean) column.clone();

			try {
				String propName = ConventionUtils.getPropName(this.conventionStrategy, column.getName(), column.isPrimaryKey());
				if(null != rowObj){
					if( rowObj instanceof Map && (( Map )rowObj).containsKey(propName)){
						propValue = (( Map )rowObj).get(propName);
					}
					else if ( rowObj instanceof BaseObject && PropertyUtils.isWriteable(rowObj, propName)) {
						propValue = PropertyUtils.getProperty(rowObj, propName);
					}
					column.setPropValue(propValue);
					columnsWithNotIgnoreNull.add(column);
					if (propValue != null) {
						columnsWithNotNullValue.add(column);
						if (StringUtils.isNotEmpty(String.valueOf(propValue)))
							columnsWithNotEmptyValue.add(column);
					}
				}
				if(null != conditionObj){
					if( conditionObj instanceof Map && (( Map )conditionObj).containsKey(propName)){
						propValue = (( Map )conditionObj).get(propName);
					}
					else if ( conditionObj instanceof BaseObject && PropertyUtils.isWriteable(conditionObj, propName)) {
						propValue = PropertyUtils.getProperty(conditionObj, propName);
					}
					conditionColumn.setPropValue(propValue);
					conditionsWithNotIgnoreNullValue.add(column);
					if (propValue != null) {
						conditionsWithNotNullValue.add(column);
						if (StringUtils.isNotEmpty(String.valueOf(propValue)))
							conditionsWithNotEmptyValue.add(column);
					}
				}
			} catch (Exception e) {
				logger.info("初始化持久层值对象的时候出现错误:" + e.getMessage());
				if (logger.isDebugEnabled()) {
					logger.debug("初始化持久层值对象的时候出现错误", e);
				}
			}
		}
		setColumnsWithNotIgnoreNull(columnsWithNotIgnoreNull);
		setColumnsWithNotEmptyValue(columnsWithNotEmptyValue);
		setColumnsWithNotNullValue(columnsWithNotNullValue);
		setConditionsWithNotEmptyValue(conditionsWithNotEmptyValue);
		setConditionsWithNotNullValue(conditionsWithNotNullValue);
		setConditionsWithNotIgnoreNullValue(conditionsWithNotIgnoreNullValue);

	}

	public TableBean getDbo() {
		return dbo;
	}

	public void setDbo(TableBean dbo) {
		this.dbo = dbo;
	}

	public Object getVo() {
		return dataObj;
	}

	public void setVo(Object vo) {
		this.dataObj = vo;
	}

	public List getColumnsWithNotEmptyValue() {
		return columnsWithNotEmptyValue;
	}

	public void setColumnsWithNotEmptyValue(List columnsWithNotEmptyValue) {
		this.columnsWithNotEmptyValue = columnsWithNotEmptyValue;
	}

	public List getColumnsWithNotNullValue() {
		return columnsWithNotNullValue;
	}

	public void setColumnsWithNotNullValue(List columnsWithNotNullValue) {
		this.columnsWithNotNullValue = columnsWithNotNullValue;
	}

	public List getConditionsWithNotEmptyValue() {
		return conditionsWithNotEmptyValue;
	}

	public void setConditionsWithNotEmptyValue(List conditionsWithNotEmptyValue) {
		this.conditionsWithNotEmptyValue = conditionsWithNotEmptyValue;
	}

	public List getConditionsWithNotNullValue() {
		return conditionsWithNotNullValue;
	}

	public void setConditionsWithNotNullValue(List conditionsWithNotNullValue) {
		this.conditionsWithNotNullValue = conditionsWithNotNullValue;
	}

	public IConventionStrategy getConventionStrategy() {
		return conventionStrategy;
	}

	public void setConventionStrategy(IConventionStrategy conventionStrategy) {
		this.conventionStrategy = conventionStrategy;
	}

	public Object getDataObj() {
		return dataObj;
	}

	public void setDataObj(Object dataObj) {
		this.dataObj = dataObj;
	}

	public List getColumnsWithNotIgnoreNull() {
		return columnsWithNotIgnoreNull;
	}

	public void setColumnsWithNotIgnoreNull(List columnsWithNotIgnoreNull) {
		this.columnsWithNotIgnoreNull = columnsWithNotIgnoreNull;
	}

}
