package com.rework.joss.persistence.convention.jdbctype;

import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 数字类型处理类
 * 
 * @author zhujj
 * @create 2008-1-24
 */
public class NumberJdbcTypeHander implements JdbcTypeHander {
	
	private static Log log = LogFactory.getLog(NumberJdbcTypeHander.class);
	
	public Object handerJdbcTypeValue(Object value) {
		if(value == null){
			return null;
		}
		if("".equals(String.valueOf(value).trim())){
			return "";
		}
		log.debug("处理数字类型值：" + value);
		//类型一致则不用处理
		if(value.getClass() == BigDecimal.class){
			return value;
		}
		return new BigDecimal(String.valueOf(value));
	}

}
