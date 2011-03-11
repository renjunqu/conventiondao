/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.rework.joss.persistence.convention;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ConvetionStrategyApplyColMap implements IConventionStrategy {

	private IConventionStrategy defaultStrategy;
	/** key:数据库字段名,value:pojo property name */
	private Map colmap = new HashMap();
	
	public ConvetionStrategyApplyColMap(IConventionStrategy defaultStrategy,
			Map colmap) {
		super();
		this.defaultStrategy = defaultStrategy;
		this.colmap = colmap;
	}

	public String getLogicIdName(String table) {
		return defaultStrategy.getLogicIdName(table);
	}

	public String translateFromColumnToProperty(String columnNam,
			boolean isPrimaryKey) {
		Map tempMap = new HashMap();
		// key(column 通通大写)
		for (Iterator iterator = colmap.keySet().iterator(); iterator.hasNext();) {
			Object key = iterator.next();
			tempMap.put(key.toString().toUpperCase(), colmap.get(key));
		}
		String customString = (String) tempMap.get(columnNam.toUpperCase());
		if(customString != null){
			return customString;
		}
		return defaultStrategy.translateFromColumnToProperty(columnNam, isPrimaryKey);
	}
	
	public String translateFromPropertyToColumn(String propertyName){
		
		for (Iterator iterator = colmap.keySet().iterator(); iterator.hasNext();) {
			Object key = iterator.next();
			if(colmap.get(key).toString().toUpperCase().equals(propertyName.toUpperCase())){
				return key.toString();
			}
		}
		return defaultStrategy.translateFromPropertyToColumn(propertyName);
	}
	
}
