package com.rework.joss.persistence.convention.id;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * 在数据库中单建表产生id的策略
 * 可以保持在单个库中的唯一性
 * @author heaven
 *
 */
public class IdTableGenerator extends JdbcDaoSupport implements IdGenerator{

	private String checkSql;

	private String createTableSql;

	private String insertSql;

	private String updateSql;

	private String selectSql;

	/**
	 * 初始化
	 * 如果没建表，那么新建
	 * 插一条记录
	 */
	public void init(){
		try{
			getJdbcTemplate().query(checkSql, new RowMapper(){
				public Object mapRow(ResultSet arg0, int arg1)
						throws SQLException {
					return null;
				}});
		}catch (DataAccessException e) {
			getJdbcTemplate().update(createTableSql);
			getJdbcTemplate().update(insertSql);
		}
	}

	public Object id(){
		getJdbcTemplate().update(updateSql);
		return getJdbcTemplate().queryForLong(selectSql);
	}

	public void setCheckSql(String checkSql) {
		this.checkSql = checkSql;
	}

	public void setCreateTableSql(String createTableSql) {
		this.createTableSql = createTableSql;
	}

	public void setInsertSql(String insertSql) {
		this.insertSql = insertSql;
	}

	public void setUpdateSql(String updateSql) {
		this.updateSql = updateSql;
	}

	public void setSelectSql(String selectSql) {
		this.selectSql = selectSql;
	}



}
