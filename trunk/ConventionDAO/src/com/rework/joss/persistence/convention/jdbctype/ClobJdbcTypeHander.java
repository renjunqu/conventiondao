package com.rework.joss.persistence.convention.jdbctype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.LobHandler;

public class ClobJdbcTypeHander implements JdbcTypeHander {
	private static Log log = LogFactory.getLog(ClobJdbcTypeHander.class);
	
	private LobHandler oracleLobHandler;

	public ClobJdbcTypeHander(LobHandler oracleLobHandler){
		this.oracleLobHandler = oracleLobHandler;
	}
	
	public Object handerJdbcTypeValue(Object value) {
		if(value == null){
			return null;
		}else{
			log.debug("处理CLOB类型值：" + value);
			return new SqlLobValue(String.valueOf(value), oracleLobHandler);
		}
	}

}
