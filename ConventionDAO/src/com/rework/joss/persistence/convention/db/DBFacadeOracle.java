package com.rework.joss.persistence.convention.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.Assert;

import com.rework.joss.persistence.convention.ConventionUtils;
import com.rework.joss.persistence.convention.ORMappingSource;
import com.rework.joss.persistence.convention.db.model.ColumnBean;
import com.rework.joss.persistence.convention.db.model.Container;
import com.rework.joss.persistence.convention.db.model.TableBean;

/**
 * 数据库操作的统一出口
 * <p>
 * notes: 这个类会在初始化一个数据库连接， 因此在实例用完了之后一定要调用detroy方 法来释放有可能没有断掉的数据库连接
 * </p>
 * 
 * @author kevin
 * 
 */
public class DBFacadeOracle implements IDBFacade {
	
	private static Log log = LogFactory.getLog(DBFacadeOracle.class);

	private DataSource dataSource;
	
	public DBFacadeOracle(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	
	/**
	 * Return the number of tables that match the schema pattern and table
	 * pattern given
	 * 
	 * @param metadata
	 * @param schemaPattern
	 * @param tablePattern
	 * @return the number of tables
	 * @throws SQLException
	 */
	private int getTableCount(final String schemaPattern,
			final String tablePattern) {
		String sql = "select count(*) as num from user_objects o " +
		"where o.object_type = 'TABLE' or o.object_type = 'VIEW'" +
		" and o.object_name like ?";
		List num = query(sql, new String[]{tablePattern}, new RowMapperCallback() {
			public Object processRow(ResultSet rs) throws SQLException {
				return new Integer(rs.getInt("num"));
			}
		});

		return ((Integer) num.get(0)).intValue();
	}

	/**
	 * Load the container with table objects that only contain the name. Start
	 * the thread or call populateTableData to load the columns and keys.
	 * 
	 * @param container
	 * @param metadata
	 * @param schemaPattern
	 * @param tablePattern
	 * @throws SQLException
	 */
	private void getTables(final Container container,
			final String schemaPattern, final String tablePattern) {

		String sql = " select o.object_name, o.object_type from user_objects o "
				+ " where (o.object_type = 'TABLE' or o.object_type = 'VIEW')"
				+ " and o.object_name like ?";

		query(sql, new String[] { tablePattern.toUpperCase() },
				new RowMapperCallback() {

					public Object processRow(ResultSet rs) throws SQLException {

						TableBean table = new TableBean(rs.getString("object_name"));
						String objectType = rs.getString("object_type");
						table.setObjectType(objectType);
						container.addTable(table.getName(), table);

						return null;
					}
				});

	}
	
	private void initTablesType(final TableBean dbTable) {

		String sql = " select o.object_name, o.object_type from user_objects o "
			+ " where (o.object_type = 'TABLE' or o.object_type = 'VIEW')"
			+ " and o.object_name = ?";
		query(sql, new String[] { dbTable.getName().toUpperCase() },
				new RowMapperCallback() {

			public Object processRow(ResultSet rs) throws SQLException {
				String objectType = rs.getString("object_type");
				dbTable.setObjectType(objectType);
				return null;
			}
		});	
	}

	/**
	 * Populate the column and key information for the tables.
	 * 
	 * @param metadata
	 * @param container
	 *            the table container
	 * @param tableName 
	 * @throws SQLException
	 */
	public void populateTableData(Container container, String tableName) {
		Assert.notNull(tableName, "target table name should not be empty");
		TableBean dbTable = getTableFromContainer(container, tableName);
		// 为container中的数据库对象集合加上类型
		initTablesType(dbTable);
		readTableColumns(dbTable);
		readTableKeys(dbTable);
		dbTable.init();
		container.fullyLoaded = true;
	}



	/**
	 * Read the columns from the DatabaseMetaData and notify the given table of
	 * the colums
	 * 
	 * @param meta
	 * @param table
	 * @throws SQLException
	 */
	private void readTableColumns(final TableBean table) {

		String sql = "select com.column_name "+
					       ",com.comments"+
					       ",col.TABLE_NAME"+
					       ",col.DATA_TYPE"+
					       ",col.NULLABLE"+
					       ",col.CHAR_LENGTH"+
					       ",col.DATA_SCALE "+ 
					  "from user_tab_columns col "+
					 "inner join user_col_comments com on col.COLUMN_NAME = com.column_name and col.TABLE_NAME = com.table_name "+
					 "where col.TABLE_NAME = ?";
		
		query(sql, new String[]{table.getName().toUpperCase()}, new RowMapperCallback() {


			public Object processRow(ResultSet columns) throws SQLException {
				
				String columnName = columns.getString("COLUMN_NAME");
				String datatype = columns.getString("data_type");
				int datasize = columns.getInt("char_length");
				int digits = columns.getInt("data_scale");
				int nullable = columns.getString("NULLABLE").equals("Y")?1:0;
				int jdbcType = ConventionUtils.getJdbcType(datatype);
				String remark = columns.getString("comments");
				ColumnBean newColumn = new ColumnBean(table, columnName, datatype,
						jdbcType, datasize, digits, nullable, remark);
				table.notifyColumn(newColumn);
				return null;
			}
		});

	}

	/**
	 * Read the primary and foreign keys from the DatabaseMetaData and notify
	 * the given table of the keys
	 * 
	 * @param table
	 * @throws SQLException
	 */
	private void readTableKeys(final TableBean table) {
		String sql = "select cs.table_name, cc.column_name  from " +
				"user_constraints cs join user_cons_columns cc on cs.constraint_name = cc.constraint_name " +
				"where cs.table_name like ? and cs.constraint_type = 'P'";
		query(sql , new String[]{table.getName().toUpperCase()}, new RowMapperCallback() {

			public Object processRow(ResultSet rs) throws SQLException {
				String columnName = rs.getString("column_name");
				table.notifyPrimaryKey(checkName(columnName));
				return null;
			}
		});
		// TODO heer view convention
		table.notifyPrimaryKey(getHeerViewLogicIdName(table.getName()));
	}
	
	public String getHeerViewLogicIdName(String table) {
		return table.substring(3).toUpperCase() + "_ID";
	}
	
	private String checkName(String s) {
		if (null == s)
			return null;
		s = ConventionUtils.stringReplace(s, "`", "");
		return s;
	}
	private TableBean getTableFromContainer(final Container container,
			String tableName) {
		TableBean dbTable = container.getTable(tableName);
		if(dbTable == null){
			dbTable = new TableBean(tableName);
			container.addTable(tableName, dbTable);
		}
		return dbTable;
	}	
	private interface RowMapperCallback {

		public Object processRow(ResultSet rs) throws SQLException;
	}
	
	/**
	 * 一个Template方法
	 * 用来处理基于PreparedStatement的查询
	 * 
	 * @param sql
	 * @param parameters
	 * @param callback
	 * @return
	 */
	private List query(String sql , Object[] parameters, final RowMapperCallback callback) {
		final List list = new ArrayList();
		JdbcTemplate jdt = new JdbcTemplate(dataSource);
		jdt.query(sql, parameters, new RowMapper(){
			public Object mapRow(ResultSet rs, int rowNum) throws SQLException{
				list.add(callback.processRow(rs));
				return null;
			}
		});
		return list;
	}

}
