package com.rework.joss.persistence.convention.db.model;


public class TableJoin {
	private ColumnBean primaryKey;
	private ColumnBean foreignKey;

	public TableJoin (ColumnBean primaryKey, ColumnBean foreignKey) {
		this.primaryKey = primaryKey;
		this.foreignKey = foreignKey;
	}
	
	/**
	 * @return
	 */
	public ColumnBean getForeignKey() {
		return foreignKey;
	}

	/**
	 * @param foreignKey
	 */
	public void setForeignKey(ColumnBean foreignKey) {
		this.foreignKey = foreignKey;
	}

	/**
	 * @return
	 */
	public ColumnBean getPrimaryKey() {
		return primaryKey;
	}

	/**
	 * @param primaryKey
	 */
	public void setPrimaryKey(ColumnBean primaryKey) {
		this.primaryKey = primaryKey;
	}
}