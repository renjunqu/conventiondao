package com.rework.joss.persistence.convention.db.model;

import org.apache.commons.lang.StringUtils;

import com.rework.joss.persistence.convention.db.IJdbcTypeRegistry;
import com.rework.joss.persistence.convention.db.OracleDialect;




/**
 * 基于特定的生成业务对BaseColumn做的扩展
 * @author kevin
 * com.rework.joss.persistence.convention.db.DBColumn.java
 */
public class ColumnBean {

	/* ~ java pojo 属性 */
	// jdbc类型
	private int jdbcType = 19820217;
	
	// 属性的值
	private Object propValue;
	
	// java 的类型
	private String javaType;
	
	// java 的类型简略形式,通常是类名(不带包名)
	private String javaTypeShortcut;
	
	// ibatis 的类型
	private String ibatisType;
	
	// 是否是java的属性
	private boolean isProperty;

	/* ~ 数据字段属性 */
	// 关联的表
	private TableBean table;

	// 字段名
	private String name;

	// 数据类型
	private String dataType;

	// 大小
	private long size;

	private int digits;

	private int nullable;
	// 列的注释
	private String metaData;

	// 是否是主键
	boolean primaryKey;
	
	boolean isAutoincrement;

	ColumnBean fkParentKey;

	protected String fkPropName;

	public ColumnBean() {
	};


	public ColumnBean(
			TableBean table, 
			String name,
			String dataType, 
			int jdbcType,
			long size, 
			int digits, 
			int nullable,
			String metaData,
			boolean isAutoCrement
			) {
		super();
		this.jdbcType = jdbcType;
		this.table = table;
		this.name = name;
		this.dataType = dataType;
		this.size = size;
		this.digits = digits;
		this.nullable = nullable;
		this.metaData = metaData;
	}


	/**
	 * Return true if this column represents a primary key and false if not
	 */
	public boolean isPrimaryKey() {
		return primaryKey;
	}

	/**
	 * Return true if this column represents a foreign key and false if not
	 */
	public boolean isForeignKey() {
		return null != fkParentKey;
	}

	public TableBean getParentTable() {
		if (null == fkParentKey)
			return null;
		else
			return fkParentKey.getTable();
	}

	/**
	 * Return true if this column can be null and false if not
	 */
	public boolean isNull() {
		return nullable == 1;
	}

	/**
	 * @return Returns the Hibernate type attribute
	 */
	public String getDataType() {
		return dataType;
	}

	/**
	 * @param dataType
	 *            The dataType to set.
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return Returns the digits.
	 */
	public int getDigits() {
		return digits;
	}

	/**
	 * @param digits
	 *            The digits to set.
	 */
	public void setDigits(int digits) {
		this.digits = digits;
	}

	/**
	 * Return the column name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the column name
	 * 
	 * @param name
	 *            The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Set the nullable value
	 * 
	 * @param nullable
	 *            The nullable to set.
	 */
	public void setNullable(int nullable) {
		this.nullable = nullable;
	}

	/**
	 * Return the size of the column
	 */
	public long getSize() {
		return size;
	}

	/**
	 * Set the size of the column
	 * 
	 * @param size
	 *            The size to set.
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * Return the table that this column belongs to
	 */
	public TableBean getTable() {
		return table;
	}

	/**
	 * Set the table that this column belongs to
	 * 
	 * @param table
	 *            The table to set.
	 */
	public void setTable(TableBean table) {
		this.table = table;
	}

	public String toString() {
		return getName() + " (" + getDataType() + ")";
	}

	/**
	 * @return Returns the metaData.
	 */
	public String getMetaData() {
		if(null != metaData){
			metaData = StringUtils.replace(metaData, "--", "");
		}
		return metaData;
	}

	/**
	 * @param metaData
	 *            The metaData to set.
	 */
	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}

	public boolean equals(Object obj) {
		if (null == obj || !(obj instanceof ColumnBean))
			return false;
		if (null == getTable().getName() || null == getName())
			return false;
		ColumnBean col = (ColumnBean) obj;
		if (null == col.getTable().getName() || null == col.getName())
			return false;
		return (col.getTable().getName().equals(getTable().getName()) && col
				.getName().equals(getName()));
	}

	private Integer hashCode;

	public int hashCode() {
		if (null == hashCode) {
			if (null == getTable().getName() || null == getName())
				return super.hashCode();
			hashCode = new Integer(new String(getTable().getName() + ":"
					+ getName()).hashCode());
		}
		return hashCode.intValue();
	}

	/**
	 * <LI><B>DATA_TYPE</B> int => SQL type from java.sql.Types
	 * 
	 * @return
	 */
	public int getJdbcType() {
		return jdbcType;
	}

	public void setJdbcType(int jdbcType) {
		this.jdbcType = jdbcType;
	}

	/**
	 * Set the Hibernate property name for this column
	 * 
	 * @param propName
	 */
	public void setPropName(String propName) {
		this.fkPropName = propName;
	}

	

	public String getIbatisType() {
		ibatisType = IJdbcTypeRegistry.getDataType(getJdbcType());
		if("NULL".equals(ibatisType)){
			ibatisType = getDataType();
			if(null != ibatisType){
				if("VARCHAR2".equals(ibatisType)){
					return "VARCHAR";
				}else if(ibatisType.startsWith("TIMESTAMP")){
					return "TIMESTAMP";
				}if(ibatisType.startsWith("NUMBER")){
					return "NUMBERIC";
				}
			}
		}
		return ibatisType;
	}

	/**
	 * 严重需要重构
	 * @return
	 */
	public String getJavaType() {
		// TODO 这里要重构掉，需要将oracle 系的操作新安排一下结构
		if (IJdbcTypeRegistry.NO_JDBC_TYPE == jdbcType) {
			javaType = (String) OracleDialect.typeNames.get(getDataType());
		} else {
			javaType = IJdbcTypeRegistry.getJavaType(getJdbcType());
		}
		
		return javaType;
	}
	
	public String getJavaTypeShortcut() {
		javaTypeShortcut = StringUtils.substringAfterLast(getJavaType(), ".");
		return javaTypeShortcut;
	}

	public Object getPropValue() {
		return propValue;
	}

	public void setPropValue(Object propValue) {
		this.propValue = propValue;
	}

	public boolean isProperty() {
		return isProperty;
	}


	public void setProperty(boolean isProperty) {
		this.isProperty = isProperty;
	}
	
	public boolean isPropValueNotNull(){
		return propValue != null;
	}
	
	public boolean isPropValueNotEmpty(){
		return propValue != null && StringUtils.isNotBlank(String.valueOf(propValue));
	}
	
	
	public boolean isAutoincrement() {
		return isAutoincrement;
	}


	public void setAutoincrement(boolean isAutoincrement) {
		this.isAutoincrement = isAutoincrement;
	}


	public Object clone(){
		return new ColumnBean(
				this.table, 
				this.name, 
				this.dataType,
				this.jdbcType, 
				this.size, 
				this.digits, 
				this.nullable,
				this.metaData,
				this.isAutoincrement);
	}
	
}