package com.rework.joss.persistence.convention;


public class QuerySetting {
	
	/** 模糊查询类型, 在前面加% */
	public static String FUZZY_TYPE_BEFORE = "FUZZY_TYPE_BEFORE";
	/** 模糊查询类型, 在后面加% */
	public static String FUZZY_TYPE_AFTER = "FUZZY_TYPE_AFTER";
	/** 模糊查询类型, 在两边加% */
	public static String FUZZY_TYPE_BOTH = "FUZZY_TYPE_BOTH";
	
	/** 模糊查询的类型 */
	private String fuzzy;

	public String getFuzzy() {
		return fuzzy;
	}

	public void setFuzzy(String fuzzy) {
		this.fuzzy = fuzzy;
	}
	
	
	
}
