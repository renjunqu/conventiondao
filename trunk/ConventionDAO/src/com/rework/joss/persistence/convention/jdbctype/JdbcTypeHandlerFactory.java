package com.rework.joss.persistence.convention.jdbctype;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.support.lob.LobHandler;

public class JdbcTypeHandlerFactory {
	
	private final Map jdbcTypeHandlerMap = new HashMap();
	
	/**
	 * Default constructor
	 */
	public JdbcTypeHandlerFactory(LobHandler oracleLobHandler) {
		register(Types.DATE, new DateJdbcTypeHander());
		register(Types.NUMERIC,new NumberJdbcTypeHander());
		register(Types.CLOB, new ClobJdbcTypeHander(oracleLobHandler));
	}


	/* Public Methods */

	/**
	 * Register (add) a type handler for a JDBC type
	 * 
	 * @param jdbcType - the JDBC type int {@link java.sql.Types}
	 * @param handler - the handler instance
	 */
	private void register(int jdbcType,JdbcTypeHander handler) {
		jdbcTypeHandlerMap.put(String.valueOf(jdbcType), handler);
	}

	/**
	 * Get a JdbcTypeHander for a class
	 * 
	 * @param jdbcType -  the JDBC type int {@link java.sql.Types}
	 * @return - the handler
	 */
	public JdbcTypeHander getTypeHandler(int jdbcType) {
		JdbcTypeHander JdbcTypeHander = (JdbcTypeHander)jdbcTypeHandlerMap.get(String.valueOf(jdbcType));
		if (JdbcTypeHander == null) {
			return new DefaultJdbcTypeHander();
		}else{
			return JdbcTypeHander;
		}
	}
}
