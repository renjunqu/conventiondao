package com.rework.joss.persistence.convention.strategy;

import javax.sql.DataSource;

import com.rework.joss.persistence.convention.db.Constants;
import com.rework.joss.persistence.convention.db.DBFactory;

/**
 * 基于不同数据库的sql处理策略
 */
public class SqlStrategyFactory {

	public static SqlStrategy getBean(DataSource dataSource) {
		
		String type = DBFactory.getDBType(dataSource);
		if(Constants.MYSQL.equals(type)){
			return new MysqlSqlStrategy();
		}else if(Constants.ORACLE.equals(type)){
			return new OracleSqlStrategy();
		}else{
			throw new RuntimeException("不支持的数据库类型!");
		}
	}


}
