package com.rework.joss.persistence.convention.id;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

/**
 * 在数据库中单建表产生id的策略
 * 可以保持在单个库中的唯一性
 * @author heaven
 *
 */
public class IdTableGenerator implements IdGenerator{
	private Log logger = LogFactory.getLog(getClass());
	
	public Object id(DataSource ds, String dbo){
		Connection con = DataSourceUtils.getConnection(ds);
		Statement ps = null;
		ResultSet rs = null;
		String id = null;
		try {
			ps = con.createStatement();
			ps.executeUpdate("REPLACE INTO table_pk_key(table_name) VALUES ('"+ dbo +"')");
			rs = ps.executeQuery("SELECT LAST_INSERT_ID()");
			if(rs.next()) {
				id = rs.getString(1);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("生成["+ dbo +"]主键:" + id);
			}
			return id;
		}
		catch (SQLException ex) {
			logger.error(dbo + "生成主键失败", ex);
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(ps);
			ps = null;
			DataSourceUtils.releaseConnection(con, ds);
			con = null;
		}
		finally {
			JdbcUtils.closeResultSet(rs);
			JdbcUtils.closeStatement(ps);
			DataSourceUtils.releaseConnection(con, ds);
		}
		return null;
	}


}
