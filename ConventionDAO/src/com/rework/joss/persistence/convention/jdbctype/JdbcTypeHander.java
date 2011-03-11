package com.rework.joss.persistence.convention.jdbctype;

/**
 * <b>数据类型转换</b>
 * <p>
 * 将某个数据转换成数据库的数据类型
 * 
 * @author zhujj
 * @create 2007-12-12
 */
public interface JdbcTypeHander {
	/**
	 * 将某个值转换成数据库类型的值
	 * @param value
	 * @return
	 */
	public Object handerJdbcTypeValue(Object value);
}
