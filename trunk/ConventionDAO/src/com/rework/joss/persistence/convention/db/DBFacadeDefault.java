package com.rework.joss.persistence.convention.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
public class DBFacadeDefault implements IDBFacade {

	private static Log log = LogFactory.getLog(DBFacadeDefault.class);

	private DataSource dataSource;
	
	DBFacadeDefault(DataSource dataSource) {
		this.dataSource = dataSource;
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
		TableBean dbTable = container.getTable(tableName);
		if(dbTable == null){
			dbTable = new TableBean(tableName);
			container.addTable(tableName, dbTable);
		}
		readTableColumns(dbTable);
		// 读取外键以以及对庆的表信息
		readTableKeys(dbTable, container);
		dbTable.init();
		container.fullyLoaded = true;
	}

	/**
	 * Read the columns from the DatabaseMetaData and notify the given table of
	 * the colums
	 * 
	 * @param table
	 * @throws SQLException
	 */
	private void readTableColumns(final TableBean table) {

		metaDataOperation(new MetadataCallback() {

			public ResultSet retrieveResutSetFromMetadata(
					DatabaseMetaData metaData) throws SQLException {
				ResultSet rs = metaData.getColumns(null, getDefaltSchemaName(metaData), getTableName(table), "%");
				return rs;
			}

			public void processRow(ResultSet columns) throws SQLException {
				String columnName = columns.getString("COLUMN_NAME");
				String datatype = columns.getString("TYPE_NAME");
				int jdbcType = columns.getInt("DATA_TYPE");
				int datasize = columns.getInt("COLUMN_SIZE");
				int digits = columns.getInt("DECIMAL_DIGITS");
				int nullable = columns.getInt("NULLABLE");
				String isAuto = columns.getString("IS_AUTOINCREMENT");
				boolean isAutocrement = false;
				if( null != isAuto && "YES".equals(isAuto.toUpperCase()) ){
					isAutocrement = true;
				}
				
				String remark = columns.getString("REMARKS");
				ColumnBean newColumn = new ColumnBean(table, columnName, datatype, jdbcType, datasize, digits, nullable, remark, isAutocrement);
				table.notifyColumn(newColumn);

			}
		});

	}

	/**
	 * Read the primary and foreign keys from the DatabaseMetaData and notify
	 * the given table of the keys
	 * 
	 * @param meta
	 * @param table
	 * @param tables
	 * @throws SQLException
	 */
	private void readTableKeys(final TableBean table, final Container container) {

		metaDataOperation(new MetadataCallback() {

			public ResultSet retrieveResutSetFromMetadata(
					DatabaseMetaData metaData) throws SQLException {
				return metaData.getPrimaryKeys(null, getDefaltSchemaName(metaData), getTableName(table));
			}

			public void processRow(ResultSet rs) throws SQLException {
				// String tableName = rs.getString("TABLE_NAME");
				String columnName = rs.getString("COLUMN_NAME");
				//TableBean table = (TableBean) tables.get(checkName(tableName));
				table.notifyPrimaryKey(checkName(columnName));
			}
		});

		metaDataOperation(new MetadataCallback() {

			public ResultSet retrieveResutSetFromMetadata(
					DatabaseMetaData metaData) throws SQLException {
				ResultSet rs  =  metaData.getImportedKeys(null, getDefaltSchemaName(metaData), getTableName(table));
				return rs;
			}

			public void processRow(ResultSet importKeys) throws SQLException {
				
				String pkTableName = importKeys.getString("PKTABLE_NAME");
				pkTableName = importKeys.getString("PKTABLE_NAME");
				String pkColumnName = importKeys.getString("PKCOLUMN_NAME");
				String fkTableName = importKeys.getString("FKTABLE_NAME");
				String fkColumnName = importKeys.getString("FKCOLUMN_NAME");
				TableBean pkTable = container.getTable(checkName(pkTableName));
				TableBean fkTable = container.getTable(checkName(fkTableName));
				if (null != pkTable && null != fkTable) {
					ColumnBean pkColumn = pkTable.getColumn(checkName(pkColumnName));
					if (null != pkColumn)
						table.notifyForeignKey(checkName(fkColumnName), pkColumn);
				}
				
			}
		});

	}

	private String checkName(String s) {
		if (null == s)
			return null;
		s = ConventionUtils.stringReplace(s, "`", "");
		return s;
	}

	private Connection getConnection() throws SQLException {
		return DataSourceUtils.getConnection(dataSource);
		// return dataSource.getConnection();
	}

	private interface MetadataCallback {

		public ResultSet retrieveResutSetFromMetadata(DatabaseMetaData metaData)
				throws SQLException;

		public void processRow(ResultSet rs) throws SQLException;
	}
	
	protected String getDefaltSchemaName(DatabaseMetaData metada){
		return null;
	}

	/**
	 * 回调接口用于处理基于 {@link DatabaseMetaData} 的操作
	 * @param callback
	 */
	private void metaDataOperation(MetadataCallback callback) {
		ResultSet rs;
		Connection conn = null;
		try {
			conn = getConnection();
			rs = callback.retrieveResutSetFromMetadata(conn.getMetaData());
			while (rs.next()) {
				callback.processRow(rs);
			}
			rs.close();
		} catch (SQLException e) {
			throw new DataAccessException(e);
		} finally {
			DataSourceUtils.releaseConnection(conn, dataSource);
			/*if (null != conn) {
				try {
					conn.close();
				} catch (SQLException e) {
					log.error("关闭连接出现异常", e);
				}
			}*/
		}

	}
	
	/**
	 * 得到表名称
	 * @param table
	 * @return
	 */
	protected String getTableName(final TableBean table) {
		return table.getName();
	}

}
