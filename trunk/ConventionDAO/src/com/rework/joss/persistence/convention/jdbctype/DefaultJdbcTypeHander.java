package com.rework.joss.persistence.convention.jdbctype;

/**
 * 缺省处理器
 * 
 * @author zhujj
 * @create 2007-12-12
 */
public class DefaultJdbcTypeHander implements JdbcTypeHander{
	//缺省不做处理
	public Object handerJdbcTypeValue(Object value){
		return value;
	}
}
