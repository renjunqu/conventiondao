package com.rework.joss.persistence.convention.db.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import com.rework.joss.persistence.convention.ConventionUtils;
import com.rework.joss.persistence.convention.IConventionStrategy;
import com.rework.joss.persistence.convention.db.Constants;


/**
 * 
 * 表示一个数据库对象
 * 注意 clone方法
 * @author zhangsh
 *
 */
public class TableBean implements Comparable,Cloneable {
	
	private Log logger = LogFactory.getLog(TableBean.class);
	
	private String name;

	private String metaData;
	// default table
	private String objectType = Constants.TABLE;

	private List columns = new ArrayList();

	private List childRelationships = new ArrayList();
	private List parentRelationships = new ArrayList();
	private List subClasses = new ArrayList();
	
	// cache
	private List oneToManyRelationships = new ArrayList();
	private List manyToManyRelationships = new ArrayList();
	private Map pKeys;
	private Map fKeys;
	private Map columnMap = new HashMap();
	private TableRelationship parentTableRelationship;
	private Boolean hasParentTable;
	
	// for java,will be refactory
	private String javaName;
	private Set imports = new HashSet();
	private String javaPackageName;
	
	/**
	 * Constructor
	 * @param name the name of the table
	 */
	public TableBean (String name) {
		setName(name);
	}
	
	// //////////for java generate end ////
	public Set getImports() {
		wrapImports();
		return imports;
	}
	
	private void wrapImports(){
		for (Iterator iter = getColumns().iterator(); iter.hasNext();) {
			ColumnBean element = (ColumnBean) iter.next();
			imports.add(element.getJavaType());	
		}
	}

	public String getJavaPackageName() {
		
		return ConventionUtils.getJavaNameCap(this.name).toLowerCase();
	}

	
	public String getFullJavaName(){
		return javaPackageName + "." + javaName;
	}
	
	public String getJavaName() {
		return ConventionUtils.getPropName(this.name);
	}

	// ////////// for java generate end ////
	
	
	public void removeColumn (String columnName) {
		ColumnBean column = getColumn(columnName);
		pKeys.remove(column);
		fKeys.remove(column);
		columnMap.remove(column.getName());
		columns.remove(column);
	}
	
	/**
	 * Notify this table of a column to be added
	 */
	public void notifyColumn (ColumnBean column) {
		column.setTable(this);
		columns.add(column);
		columnMap.put(column.getName().toUpperCase(), column);
	}

	/**
	 * 得到在java pojo中和数据库具有对应关系的字段信息集合
	 * @return
	 */
	public List getColumnsInPojo(){
		List tempList = new ArrayList();
		for (Iterator it = this.columns.iterator(); it.hasNext();) {
			ColumnBean c = (ColumnBean) it.next();
			if(c.isProperty()){
				tempList.add(c);
			}
		}
		return tempList;
	}
	
	/**
	 * 根据属性名取出对应的列,这个依赖于转换的规则
	 * @param propertyName
	 * @param convetionStrategy 转换规则
	 * @return
	 */
	public ColumnBean getColumnByProp(String propertyName, IConventionStrategy convetionStrategy) {
		Assert.notNull(propertyName);
		// 遍历全部的字段,把字段名转成java形式如果能匹配上那么则返回
		for (Iterator it = columns.iterator(); it.hasNext();) {
			ColumnBean c = (ColumnBean) it.next();
			if(propertyName.equals(ConventionUtils.getPropName(convetionStrategy, c.getName(), c.isPrimaryKey()))){
				return c;
			}
		}
		return null;
	}
	
	/**
	 * This should be called after all tables have been loaded.
	 */
	public void init () {
		if (null == pKeys) pKeys = new HashMap();
		if (null == fKeys) fKeys = new HashMap();
		loadRelationships();
		getParentTableRelationship();
		for (Iterator i=childRelationships.iterator(); i.hasNext(); ) {
			TableRelationship relationship = (TableRelationship) i.next();
			relationship.getParentTable().notifyChildRelationship(relationship);
		}
	}

	/**
	 * Load all of the relationships based on the foreign keys within this table
	 */
	private void loadRelationships () {
		Map usedKeys = new HashMap(fKeys.size());
		// load the single keys
		for (Iterator i=fKeys.values().iterator(); i.hasNext(); ) {
			ColumnBean column = (ColumnBean) i.next();
			if (column.fkParentKey.getTable().getPkColumns().size() == 1) {
				TableRelationship tr = new TableRelationship(column.fkParentKey.getTable(), this);
				tr.addJoin(new TableJoin(column.fkParentKey, column));
				childRelationships.add(tr);
				usedKeys.put(column.getName(), column);
			}
		}
		// load the clustered keys
		if (usedKeys.size() != fKeys.size()) {
			// TODO: implement the clustered FK functionality
		}
	}

	/**
	 * Return the TableRelationship associated with the parent class in a subclass relationship
	 * or null if N/A.
	 * @return the parent TableRelationship
	 */
	public TableRelationship getParentTableRelationship () {
		if (null == hasParentTable) {
			TableRelationship keyRel = null;
			for (Iterator i=childRelationships.iterator(); i.hasNext(); ) {
				TableRelationship tableRelationship = (TableRelationship) i.next();
				if (tableRelationship.getJoins().size() == 1
						&& ((TableJoin) tableRelationship.getJoins().get(0)).getForeignKey().isPrimaryKey()) {
					if (null == keyRel) keyRel = tableRelationship;
					else {
						keyRel = null;
						break;
					}
				}
			}
			if (null == keyRel) hasParentTable = Boolean.FALSE;
			else hasParentTable = Boolean.TRUE;
			parentTableRelationship = keyRel;
		}
		return parentTableRelationship;
	}

	/**
	 * Notify this table of a primary key
	 */
	public void notifyPrimaryKey (String columnName) {
		ColumnBean column = (ColumnBean) columnMap.get(columnName.toUpperCase());
		if (null != column && columnMap.containsKey(columnName.toUpperCase())) {
			if (null == pKeys) {
				pKeys = new HashMap();
			}
			pKeys.put(column.getName(), column);
			column.primaryKey = true;
		}
	}

	/**
	 * Notify this table of a column that is a foreign key to another table
	 * @param columnName the foreign key column name on the child table
	 * @param parentKey the primary key column on the parent table
	 */
	public void notifyForeignKey (String columnName, ColumnBean parentKey) {
		ColumnBean column = (ColumnBean) columnMap.get(columnName);
		if (null != column) {
			column.fkParentKey = parentKey;
			if (null == fKeys) fKeys = new HashMap();
			fKeys.put(column.getName(), column);
		}
	}

	/**
	 * Notify the parent table of the child relationship which will result in some type of collection
	 * @param relationship the table relationship
	 */
	public void notifyChildRelationship (TableRelationship relationship) {
		if (null != relationship.getChildTable().getParentTableRelationship()
				&& relationship.equals(relationship.getChildTable().getParentTableRelationship())) return;
		if (relationship.isOneToManyRelationship()) {
			oneToManyRelationships.add(relationship);
		}
		else if (relationship.isManyToManyRelationship()) {
			manyToManyRelationships.add(relationship);
		}
	}

	/**
	 * Notify the parent table of a child subclass to the parent table
	 * @param subclassRelationship
	 */
	public void notifySubclass (TableRelationship subclassRelationship) {
		subClasses.add(subclassRelationship);
	}

	/**
	 * Return a List of TableRelationships representing the subclasses of this class 
	 * @return a List of TableRelationships
	 */
	public List getSubClasses() {
		return subClasses;
	}

	/**
	 * Convienance method to see if this table has any subclasses
	 */
	public boolean hasSubClasses () {
		return getSubClasses().size() > 0;
	}

	/**
	 * Convienance method to determine if this table has a composite key
	 */
	public boolean hasCompositeKey () {
		return getPkColumns().size() > 1;
	}

	/**
	 * Convienance method to determine if this table has a composite key and no other fields
	 */
	public boolean isCompositeKeyOnly () {
		return (getPkColumns().size() > 1 && getPkColumns().size() == getColumns().size());
	}

	/**
	 * Return true if the column is a foreign key and false if not
	 * @param column
	 * @return
	 */
	public boolean isForeignKey (ColumnBean column) {
		if (null == fKeys) return false;
		else return (null != fKeys.get(column.getName()));
	}

	/**
	 * Return a List of foreign key columns
	 * @return a List of ColumnBean objects
	 */
	public Collection getFKColumns () {
		if (null == fKeys) return new ArrayList(0);
		return fKeys.values();
	}
	
	/**
	 * Return the relationship associates with the given column or null if N/A
	 * @param column
	 * @return
	 */
	public TableRelationship getRelationship (ColumnBean column) {
		for (Iterator i = oneToManyRelationships.iterator(); i.hasNext(); ) {
			TableRelationship rel = (TableRelationship) i.next();
			for (Iterator j = rel.getJoins().iterator(); j.hasNext(); ) {
				TableJoin join = (TableJoin) j.next();
				if (join.getForeignKey().getName().equals(column.getName())) return rel;
			}
		}
		for (Iterator i = manyToManyRelationships.iterator(); i.hasNext(); ) {
			TableRelationship rel = (TableRelationship) i.next();
			for (Iterator j = rel.getJoins().iterator(); j.hasNext(); ) {
				TableJoin join = (TableJoin) j.next();
				if (join.getForeignKey().getName().equals(column.getName())) return rel;
			}
		}
		return null;
	}

	/**
	 * Return all of the columns associated with the primary key
	 * @return a list of ColumnBean objects
	 */
	public Collection getPkColumns () {
		if (null == pKeys) return new ArrayList(0);
		return pKeys.values();
	}
	
	/**
	 * 得到主键,前提是有且只有一个主键
	 * @return
	 */
	public ColumnBean getPkColumn(){
		if(getPkColumns().size() == 0){
			throw new RuntimeException("expect one primary key but ["+this.name+"] has none!");
		}else if(getPkColumns().size() > 1){
			throw new RuntimeException("expect one primary key but ["+this.name+"]has many!");
		}else{
			return ((ColumnBean[])getPkColumns().toArray(new ColumnBean[0]))[0];
		}
	}

	/**
	 * Return all of the one-to-many relationships
	 * @Return a List of TableRelationship objects
	 */
	public List getOneToManyRelationships () {
		return oneToManyRelationships;
	}

	/**
	 * Return all of the many-to-many relationships
	 * @Return a List of TableRelationship objects
	 */
	public List getManyToManyRelationships () {
		return manyToManyRelationships;
	}
	

	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * 包的假名
	 * <p>
	 * 例如,xg_student_resume
	 * 我们在生成包的时候可能是这样的规则
	 * com.rework.xg.student
	 * <p>
	 * 因此我们取 基本包名 + 前两级做为包名
	 * 例如 com.rework(基本包名) + xg.student
	 * @return
	 */
	public String getPackageAlia(){
		Assert.notNull(this.name);
		String strs[] = StringUtils.split(this.name, '_');
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 2; i++) {
			sb.append(".");
			sb.append(strs[i]);
		}
		return sb.toString();
	}
	
	public String getClassName() {
		return ConventionUtils.getJavaNameCap(getName());
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Return all columns that belong to this table
	 * @return a list of ColumnBean objects
	 */
	public List getColumns () {
		return columns;
	}
	
	/**
	 * 返回普通的列(排除了主键)
	 * @return
	 */
	public List getGeneralColumns(){
		List generalColumns = new ArrayList();
		generalColumns.addAll(this.columns);
		generalColumns.removeAll(getPkColumns());
		return generalColumns;
	}

	/**
	 * Return the column matching the given name or null if no match if found
	 * @param columnName the column name
	 */
	public ColumnBean getColumn (String columnName) {
		return (ColumnBean) columnMap.get(columnName.toUpperCase());
	}

	/**
	 * Return all child (foreign key) relationships
	 * @return a list of TableRelationship objects
	 */
	public List getChildRelationships () {
		return childRelationships;
	}
	/**
	 * @return Returns the metaData.
	 */
	public String getMetaData() {
		return metaData;
	}
	/**
	 * @param metaData The metaData to set.
	 */
	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}
	
	public int compareTo(Object arg0) {
		if (null == arg0 || !(arg0 instanceof TableBean)) return -1;
		else return getName().compareTo(((TableBean) arg0).getName());
	}
	
	/**
	 * 得到数据库对象类型
	 * @see org.easymanage.generator.Constants#TABLE
	 * @see org.easymanage.generator.Constants#VIEW
	 * @return
	 */
	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public List getParentRelationships() {
		return parentRelationships;
	}

	public void setParentRelationships(List parentRelationships) {
		this.parentRelationships = parentRelationships;
	}


	public ColumnBean getPojoColumnByColumnName(String colName) {
		if(columnMap == null){
			return null;
		}else if(columnMap.containsKey(colName)){
			return (ColumnBean)columnMap.get(colName);
		}else if(columnMap.containsKey(colName.toUpperCase())) {
			return (ColumnBean)columnMap.get(colName.toUpperCase());
		}else if(columnMap.containsKey(colName.toLowerCase())) {
			return (ColumnBean)columnMap.get(colName.toLowerCase());
		}
		return null;
	}

	/**
	 * 深层拷贝
	 * @see java.lang.Object#clone()
	 */
	public Object clone(){
		TableBean cloneTable = new TableBean(this.name);
		cloneTable.metaData = this.metaData;
		cloneTable.objectType = this.objectType;
		if(columns != null){
			for(int i = 0; i < columns.size(); i++) {
				ColumnBean orig = (ColumnBean)columns.get(i);
				ColumnBean clone = (ColumnBean)orig.clone();
				cloneTable.notifyColumn(clone);
				if(orig.isPrimaryKey()) {
					cloneTable.notifyPrimaryKey(clone.getName());
				}
				if(this.isForeignKey(orig)) {
					cloneTable.notifyForeignKey(clone.getName(), clone);
				}
			}
		}
		if(parentRelationships != null){
			cloneTable.parentRelationships = parentRelationships.subList(0, parentRelationships.size());
		}
		if(this.childRelationships != null){
			cloneTable.childRelationships = childRelationships.subList(0, childRelationships.size());
		}
		if(this.subClasses != null){
			cloneTable.subClasses = subClasses.subList(0, subClasses.size());
		}
		if(this.oneToManyRelationships != null){
			cloneTable.oneToManyRelationships = oneToManyRelationships.subList(0, oneToManyRelationships.size());
		}
		if(this.oneToManyRelationships != null){
			cloneTable.manyToManyRelationships = manyToManyRelationships.subList(0, manyToManyRelationships.size());
		}
		cloneTable.parentTableRelationship = parentTableRelationship;
		return cloneTable;
	}
}