/*
 *  Copyright 2004 Clinton Begin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.rework.joss.persistence.convention.db;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Singleton
 */
public class IJdbcTypeRegistry {

	public static final int NO_JDBC_TYPE = 19820217;

	/**
	 * Value for the unknown type
	 */
	public static final int UNKNOWN_TYPE = -99999999;

	private static final Map IBATIS_TYPE_MAP = new HashMap();

	private static final Map JDBC_TYPE_MAP = new HashMap();

	private static final Map JAVA_TYPE_MAP = new HashMap();
	
	/**
	 * Value for a JDBC 3.o datalink type
	 */
	public final static int JDBC_30_DATALINK = 70;

	/**
	 * Value for a JDBC 3.o boolean type
	 */
	public final static int JDBC_30_BOOLEAN = 16;

	static {
		initializeTypes();
		initJdbcTypes();
		registerJavaType();
	}

	private IJdbcTypeRegistry() {
	}

	private static void setType(String name, int value) {
		IBATIS_TYPE_MAP.put(name, new Integer(value));
	}

	/**
	 * Looks up a type by name, and returns it's int value (from java.sql.Types)
	 * 
	 * @param name -
	 *            the type name
	 * 
	 * @return - the int value (from java.sql.Types)
	 */
	public static int getType(String name) {
		if (name == null)
			return UNKNOWN_TYPE;
		Integer i = (Integer) IBATIS_TYPE_MAP.get(name);
		if (i != null) {
			return i.intValue();
		} else {
			return UNKNOWN_TYPE;
		}
	}

	/**
	 * 得到jdbc data_type
	 * @param jdbcType
	 * @return
	 */
	public static String getDataType(int jdbcType) {
		return (String) JDBC_TYPE_MAP.get(new Integer(jdbcType));
	}

	private static void initJdbcTypes() {
		for (Iterator iter = IBATIS_TYPE_MAP.keySet().iterator(); iter.hasNext();) {
			Object key = (Object) iter.next();
			JDBC_TYPE_MAP.put(IBATIS_TYPE_MAP.get(key), key);
		}
	}

	private static void initJavaTypes() {

	}

	private static void initializeTypes() {
		setType("ARRAY", Types.ARRAY);
		setType("BIGINT", Types.BIGINT);
		setType("BINARY", Types.BINARY);
		setType("BIT", Types.BIT);
		setType("BLOB", Types.BLOB);
		setType("BOOLEAN", JDBC_30_BOOLEAN);
		setType("CHAR", Types.CHAR);
		setType("CLOB", Types.CLOB);
		setType("DATALINK", JDBC_30_DATALINK);
		setType("DATE", Types.DATE);
		setType("DECIMAL", Types.DECIMAL);
		setType("DISTINCT", Types.DISTINCT);
		setType("DOUBLE", Types.DOUBLE);
		setType("FLOAT", Types.FLOAT);
		setType("INTEGER", Types.INTEGER);
		setType("JAVA_OBJECT", Types.JAVA_OBJECT);
		setType("LONGVARBINARY", Types.LONGVARBINARY);
		setType("LONGVARCHAR", Types.LONGVARCHAR);
		setType("NULL", Types.NULL);
		setType("NUMERIC", Types.NUMERIC);
		setType("OTHER", Types.OTHER);
		setType("REAL", Types.REAL);
		setType("REF", Types.REF);
		setType("SMALLINT", Types.SMALLINT);
		setType("STRUCT", Types.STRUCT);
		setType("TIME", Types.TIME);
		setType("TIMESTAMP", Types.TIMESTAMP);
		setType("TINYINT", Types.TINYINT);
		setType("VARBINARY", Types.VARBINARY);
		setType("VARCHAR", Types.VARCHAR);

		setType("CH", Types.CHAR);
		setType("VC", Types.VARCHAR);

		setType("DT", Types.DATE);
		setType("TM", Types.TIME);
		setType("TS", Types.TIMESTAMP);

		setType("NM", Types.NUMERIC);
		setType("II", Types.INTEGER);
		setType("BI", Types.BIGINT);
		setType("SI", Types.SMALLINT);
		setType("TI", Types.TINYINT);
		
		setType("DC", Types.DECIMAL);
		setType("DB", Types.DOUBLE);
		setType("FL", Types.FLOAT);

		setType("ORACLECURSOR", -10);

	}

	
	
	public static String getJavaType(int jdbcType) {
		
		String javaType = (String) JAVA_TYPE_MAP.get(String.valueOf(jdbcType));
		return null==javaType?String.class.getName():javaType;
	}

	public static void registerJavaType(){
		JAVA_TYPE_MAP.put(String.valueOf(Types.BIT), Integer.class.getName());
		JAVA_TYPE_MAP.put(String.valueOf(Types.BIGINT), Integer.class.getName());
		JAVA_TYPE_MAP.put(String.valueOf(Types.SMALLINT), Integer.class.getName());
		JAVA_TYPE_MAP.put(String.valueOf(Types.TINYINT), Integer.class.getName());
		JAVA_TYPE_MAP.put(String.valueOf(Types.INTEGER), Integer.class.getName());
		JAVA_TYPE_MAP.put(String.valueOf(Types.CHAR), String.class.getName());
		JAVA_TYPE_MAP.put(String.valueOf(Types.VARCHAR), String.class.getName());
		JAVA_TYPE_MAP.put(String.valueOf(Types.FLOAT), Float.class.getName());
		JAVA_TYPE_MAP.put(String.valueOf(Types.DOUBLE), Double.class.getName());
		JAVA_TYPE_MAP.put(String.valueOf(Types.DATE), Date.class.getName());
		JAVA_TYPE_MAP.put(String.valueOf(Types.TIME), Time.class.getName());
		JAVA_TYPE_MAP.put(String.valueOf(Types.TIMESTAMP), Timestamp.class.getName());
		JAVA_TYPE_MAP.put(String.valueOf(Types.VARBINARY), String.class.getName());
		JAVA_TYPE_MAP.put(String.valueOf(Types.VARBINARY), String.class.getName());
		JAVA_TYPE_MAP.put(String.valueOf(Types.NUMERIC), Integer.class.getName());
		JAVA_TYPE_MAP.put(String.valueOf(Types.BLOB), String.class.getName());
		JAVA_TYPE_MAP.put(String.valueOf(Types.CLOB), String.class.getName());
	}
	
}
