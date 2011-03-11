package com.rework.joss.persistence.convention.db.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;



public class TableRelationship {

	private TableBean parentTable;
	private TableBean childTable;
	private List joins;

	public TableRelationship (TableBean parentTable, TableBean childTable) {
		this.parentTable = parentTable;
		this.childTable = childTable;
	}

	// cache
	private Boolean parentTableRelationship;
	private ColumnBean manyToManyChildColumn;
	private ColumnBean manyToManyAltChildColumn;
	
	public TableBean getChildTable() {
		return childTable;
	}
	public void setChildTable(TableBean childTable) {
		this.childTable = childTable;
	}
	public List getJoins() {
		if (null == joins) joins = new ArrayList();
		return joins;
	}
	public void addJoin(TableJoin join) {
		getJoins().add(join);
	}
	public void setJoins(List joins) {
		this.joins = joins;
	}
	public TableBean getParentTable() {
		return parentTable;
	}
	public void setParentTable(TableBean parentTable) {
		this.parentTable = parentTable;
	}

	/**
	 * Return true if the number of joins associated with the relationship equal the amount
	 * of primary keys contained in the parent table.
	 */
	public boolean isComplete() {
		return parentTable.getPkColumns().size() == joins.size();
	}

	/**
	 * Return true if this relation represents a subclass relationshp and false if not
	 */
	public boolean isParentTableRelationship() {
		if (parentTable.getPkColumns().size() == childTable.getPkColumns().size()) {
			HashMap matchedColumns = new HashMap();
			for (Iterator i=joins.iterator(); i.hasNext(); ) {
				TableJoin join = (TableJoin) i.next();
				if (join.getForeignKey().isPrimaryKey()) {
					matchedColumns.put(join.getPrimaryKey().getName(), join.getForeignKey());
				}
			}
			boolean allColumnsMatch = true;
			for (Iterator i=parentTable.getPkColumns().iterator(); i.hasNext(); ) {
				ColumnBean column = (ColumnBean) i.next();
				if (null == matchedColumns.get(column.getName())) {
					allColumnsMatch = false;
					break;
				}
			}
			parentTableRelationship = new Boolean(allColumnsMatch);
		}
		return parentTableRelationship.booleanValue();
	}

	/**
	 * Return true if this relationship reprents a many-to-many relationship and false otherwise
	 */
	public boolean isManyToManyRelationship () {
		ColumnBean column = null;
		int count = 0;
		for (Iterator i=getChildTable().getPkColumns().iterator(); i.hasNext(); ) {
			column = (ColumnBean) i.next();
			if (column.isForeignKey()) count++;
			else return false;
		}
		return count == 2;
	}

	/**
	 * Return true if this relationship reprents a one-to-many relationship and false otherwise
	 */
	public boolean isOneToManyRelationship () {
		return (!isManyToManyRelationship());
	}

	/**
	 * Return the opposite end of the many-to-many from the parent table of this relationshp
	 * or null if this relationship is not a many-to-many
	 */
	public TableBean getManyToManyTable () {
		if (null != getManyToManyAltChildColumn()) return getManyToManyAltChildColumn().fkParentKey.getTable();
		else return null;
	}

	/**
	 * Return the child column pointing to the opposite end of the many-to-many relationship
	 */
	public ColumnBean getManyToManyChildColumn () {
		if (null == manyToManyChildColumn) {
			if (!isManyToManyRelationship()) return null;
			for (Iterator i=childTable.getPkColumns().iterator(); i.hasNext(); ) {
				ColumnBean col = (ColumnBean) i.next();
				if (col.isForeignKey() && col.fkParentKey.getTable().getName().equals(parentTable.getName())) {
					manyToManyChildColumn = col;
					break;
				}
			}
		}
		return manyToManyChildColumn;
	}

	public ColumnBean getManyToManyAltChildColumn () {
		if (null == manyToManyAltChildColumn) {
			if (!isManyToManyRelationship()) return null;
			for (Iterator i=childTable.getPkColumns().iterator(); i.hasNext(); ) {
				ColumnBean col = (ColumnBean) i.next();
				if (col.isForeignKey() && !col.fkParentKey.getTable().getName().equals(parentTable.getName())) {
					manyToManyAltChildColumn = col;
					break;
				}
			}
		}
		return manyToManyAltChildColumn;
	}

	/**
	 * Return the property name that should be associated with this list
	 */
	/*public String getListName () {
		if (isManyToManyRelationship()) return getManyToManyListName();
		else return getOneToManyListName();
	}

	private String getOneToManyListName () {

		int count = 0;
		for (Iterator i=parentTable.getOneToManyRelationships().iterator(); i.hasNext(); ) {
			if (((TableRelationship) i.next()).getChildTable().getName().equals(childTable.getName())) {
				count ++;
			}
		}
		String name = null;
		if (count == 1) {
			name = EGUtil.firstLetterUpper(getListName(childTable.getClassName()));
		}
		else {
			StringBuffer sb = new StringBuffer();
			for (Iterator i=joins.iterator(); i.hasNext(); ) {
				if (sb.length() > 0) sb.append("And");
				TableJoin join = (TableJoin) i.next();
				sb.append(join.getForeignKey().getPropName());
			}
			name = EGUtil.firstLetterUpper(getListName(childTable.getClassName())) + "By" + sb.toString();
		}
		String javaName = EGUtil.getJavaName(childTable.getName());
		if (name.startsWith(javaName) && Character.isUpperCase(name.toCharArray()[javaName.length()])) {
			name = EGUtil.firstLetterUpper(getListName(name.substring(javaName.length(), name.length())));
		}

		return name;
	}

	private String getManyToManyListName () {

		int count = 0;
		for (Iterator i=parentTable.getManyToManyRelationships().iterator(); i.hasNext(); ) {
			TableRelationship rel = (TableRelationship) i.next();
			if (rel.getManyToManyTable().getName().equals(getManyToManyTable().getName())) {
				count ++;
			}
		}
		String name = null;
		if (count == 1) {
			name = EGUtil.firstLetterUpper(getManyToManyTable().getClassName());
		}
		
		name = getListName(name);

		return name;
	}

	private String getListName (String name) {
		if (null == name) return null;
		else if (name.endsWith("y")) {
			return name.substring(0, name.length()-1) + "ies";
		}
		else if (name.endsWith("s")) {
			return name;
		}
		else return name + "s";
	}*/
}