package com.rework.joss.persistence.convention.db;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.rework.joss.persistence.convention.BaseRuntimeException;


/**
 * 获得操作数据库的类
 * @author zhangsh
 *
 */
public class DBFactory {
	static IDBFacade facade;
	static String driverName;
	
	private static Log log = LogFactory.getLog(DBFactory.class);
	/**
	 * 根据不同的数据库类型应用不同的实现
	 * 
	 * @param dbType
	 * @return
	 */
	static public IDBFacade getDBFacade(DataSource dataSource) {
		String dbType = getDBType(dataSource);
		if (null == facade) {
			if (Constants.ORACLE.equals(dbType)) {
				facade = new DBFacadeOracle(dataSource);
			} else if (Constants.MYSQL.equals(dbType)) {
				facade = new DBFacadeDefault(dataSource);
			}
		}
		return facade;

	}
	
	/**
	 * 
	 * @param dataSource
	 * @return {@link Constants}
	 */
	static public String getDBType(DataSource dataSource) {
		
		if(StringUtils.isEmpty(driverName)){
			Connection con = null;
			try {
				// con = dataSource.getConnection();
				con = DataSourceUtils.getConnection(dataSource);
				driverName = con.getMetaData().getDriverName();
			}
			catch (SQLException ex) {
				// Release Connection early, to avoid potential connection pool deadlock
				// in the case when the exception translator hasn't been initialized yet.
				throw new BaseRuntimeException("获得连接出错！", ex);
			}
			finally {
				DataSourceUtils.releaseConnection(con, dataSource);
			}	
		}
		
		if (driverName.toLowerCase().indexOf(Constants.ORACLE) >= 0) {
			return Constants.ORACLE;
		}if (driverName.toLowerCase().indexOf(Constants.MYSQL) >= 0) {
			return Constants.MYSQL;
		} else {
			throw new RuntimeException("不支持的数据库类型:"+driverName);
		}

	}

}
