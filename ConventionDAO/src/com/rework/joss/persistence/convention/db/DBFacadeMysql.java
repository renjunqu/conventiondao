package com.rework.joss.persistence.convention.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.util.Assert;

import com.rework.joss.persistence.convention.ConventionUtils;
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
public class DBFacadeMysql implements IDBFacade {
	
	private static Log log = LogFactory.getLog(DBFacadeMysql.class);

	private DataSource dataSource;
	
	public DBFacadeMysql(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	private String getSchemaName(){
		
		Connection conn = null;
		try {
			conn = this.dataSource.getConnection();
			return conn.getCatalog();
		} catch (SQLException e) {
			throw new DataAccessException(e);
		} finally {
			DataSourceUtils.releaseConnection(conn, dataSource);
		}
	}
	
	private void initTablesType(final TableBean dbTable) {

		String sql = "select 'TABLE' as object_type from tables where table_type = 'BASE TABLE' and table_schema = '"+ getSchemaName() +"' and table_name = ?"+ 
					 "union all" +
					 "select 'VIEW' as object_type from views where table_schema = '"+ getSchemaName() +"' and table_name = ?";
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

		String sql = "select c.column_name,c.column_comment,c.table_name,c.data_type,c.is_nullable,c.character_maximum_length,c.numeric_scale " +
				" from columns c where c.table_schema = '"+ getSchemaName() +"' and table_name = ?";
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
						jdbcType, datasize, digits, nullable, remark, false);
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
		String sql = "select c.table_name, c.column_name from columns c " +
				"where c.table_schema = '"+ getSchemaName() +"' and table_name = ? and column_key = 'PRI';";
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
