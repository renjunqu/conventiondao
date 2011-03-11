package com.rework.joss.persistence.convention.db;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

public class OracleDialect {

	public static Map typeNames = new HashMap();


	static {
		typeNames.put("BLOB", String.class.getName());
		typeNames.put("CHAR", String.class.getName());
		typeNames.put("CLOB", String.class.getName());
		typeNames.put("DATE", Date.class.getName());
		typeNames.put("FLOAT", Float.class.getName());

		typeNames.put("LONG", Long.class.getName());
		typeNames.put("NUMBER", Integer.class.getName());

		typeNames.put("TIMESTAMP", Date.class.getName());
		typeNames.put("VARCHAR2", String.class.getName());
		
	}

}
