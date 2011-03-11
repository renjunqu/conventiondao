package com.rework.joss.persistence.convention.db.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * 存放 表信息及其它必要信息的 容器
 * 注意:这里缓存的表信息只包含表结构信息，不包含任何与具体数据有关的信息
 */
public class Container {

	/**
	 * {tablename, {@link TableBean}}
	 */
	private Map tables = new HashMap();
	
	public boolean fullyLoaded;
	private String packageName;
	private String javaPackageName;
	private Properties properties = new Properties();
	
	public Properties getProperties() {
		return properties;
	}
	public void setProperties(Properties properties) {
		this.properties = properties;
	}
	public Map getTables() {
		return tables;
	}
	public void setTables(Map tables) {
		this.tables = tables;
	}
	public boolean isFullyLoaded() {
		return fullyLoaded;
	}
	public String getProperty (String propertyName) {
		return properties.getProperty(propertyName);
	}
	public String getPackageName () {
		return packageName;
	}
	public void setPackageName (String packageName) {
		this.packageName = packageName;
	}

	public void addTable(String tableName , TableBean table){
		if(null == this.tables){
			tables = new HashMap();
		}
		tables.put(tableName, table);
	}
	public String getJavaPackageName() {
		return javaPackageName;
	}
	public void setJavaPackageName(String javaPackageName) {
		this.javaPackageName = javaPackageName;
	}
	public boolean hasTable(String dbo) {
		return tables.containsKey(dbo.toUpperCase()) || tables.containsKey(dbo);
	}
	public TableBean getTable(String dbo) {
		TableBean result = (TableBean)tables.get(dbo.toUpperCase());
		if(result != null){
			return result;
		}else{
			return (TableBean) tables.get(dbo);
		}
	}

	
}
