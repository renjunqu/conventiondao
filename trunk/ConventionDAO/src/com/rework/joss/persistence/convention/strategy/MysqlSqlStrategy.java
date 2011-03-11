package com.rework.joss.persistence.convention.strategy;

public class MysqlSqlStrategy implements SqlStrategy {

	public String paginate(String sqlTemplate) {

		/*
		String wrapSql = "select * from ("+ sqlTemplate +") lim limit " +
				"<#if begin?? && end??>	${begin}, ${end}" +
				"<#elseif begin??>		${begin},-1" +
				"<#elseif end??>		${end}" +
				"</#if>";
		*/
		String wrapSql = sqlTemplate +
		"<#if begin?? && interval?? >	limit ${begin}, ${interval}" +
		"<#elseif begin??>		limit ${begin},-1" +
		"<#elseif interval??>		limit ${interval}" +
		"</#if>";
		
		return wrapSql;
	}

}
