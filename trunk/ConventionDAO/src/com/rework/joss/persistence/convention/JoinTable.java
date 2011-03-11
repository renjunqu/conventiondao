package com.rework.joss.persistence.convention;

import org.apache.commons.lang.StringUtils;

import com.rework.joss.persistence.convention.db.model.TableBean;

/**
 * 
 * @author zhangsh
 *
 */
public class JoinTable {

	public static final String LEFT_JOIN = " left join ";

	public static final String INNER_JOIN = " join ";

	public static final String RIGHT_JOIN = " right join ";

	private String tableName;
	
	private String joinColumnName;
	
	private String primaryKeyName;
	
	private String joinType;
	
	private String pojoClass;
	
	private String orderBy;
	
	private String whereClause;
	
	private TableBean tableBean;
	
	/**
	 * 被写入的POJO属性
	 */
	private String targetProperty;
	
	public JoinTable(String tableName, String joinColumnName,
			String primaryKeyName, String joinType) {
		this.tableName = tableName;
		this.joinColumnName = joinColumnName;
		this.primaryKeyName = primaryKeyName;
		this.joinType = joinType;
	}

	public String wrapSql(String sql) {
		// 父表要放在前面
		sql = "select b.* from " 
			+ "("+ sql +") as a " + this.joinType + tableName +" as b " 
			+ "on b." + joinColumnName + " = a." + primaryKeyName;
		if(StringUtils.isNotBlank(getWhereClause())) {
			sql += " and " + getWhereClause();
		}
		if( StringUtils.isNotBlank( getOrderBy() ) ){
			// sql += " order by ";
			sql += " ";
			sql += getOrderBy();
		}
 		return sql;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getJoinColumnName() {
		return joinColumnName;
	}

	public void setJoinColumnName(String joinColumnName) {
		this.joinColumnName = joinColumnName;
	}

	public String getPrimaryKeyName() {
		return primaryKeyName;
	}

	public void setPrimaryKeyName(String primaryKeyName) {
		this.primaryKeyName = primaryKeyName;
	}

	public String getJoinType() {
		return joinType;
	}

	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}

	public String getPojoClass() {
		return pojoClass;
	}

	public void setPojoClass(String pojoClass) {
		this.pojoClass = pojoClass;
	}

	public TableBean getTableBean() {
		return tableBean;
	}

	public void setTableBean(TableBean tableBean) {
		this.tableBean = tableBean;
	}

	public String getTargetProperty() {
		return targetProperty;
	}

	public void setTargetProperty(String targetProperty) {
		this.targetProperty = targetProperty;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public String getWhereClause() {
		return whereClause;
	}

	public void setWhereClause(String whereClause) {
		this.whereClause = whereClause;
	}
}
