package com.rework.joss.persistence.convention.strategy;

public class OracleSqlStrategy implements SqlStrategy {

	public String paginate(String sqlTemplate) {
		/*
		 * 这个sql在分页时有问题，现改成下面的sql，效率比原先低，待优化
		String wrapSql = 
				"<#if begin?? && end??>" +
				"	select * from (select IQ.*,rownum as z_r_n from (" +
				"<#elseif begin?? || end??>" +
				"	select IQ.*,rownum as z_r_n from (" +
				"</#if>"
				+ sqlTemplate +
				"<#if begin?? && end??>" +
				"	)IQ where rownum < ${end}) where z_r_n > ${begin}" +
				"<#elseif begin??>" +
				"	)IQ where rownum > ${begin}" +
				"<#elseif end??>" +
				"	)IQ where rownum < ${end}" +
				"</#if>";*/
		String wrapSql = 
			"<#if begin?? || end??>" +
			"	select * from (select IQ.*,rownum as z_r_n from ("  +
			"</#if>"  +
			sqlTemplate +
			"<#if begin?? && end??>" +
			"	)IQ) where z_r_n < ${end} and z_r_n > ${begin}" +
			"<#elseif begin??>" +
			"	)IQ) where z_r_n > ${begin}" +
			"<#elseif end??>" +
			"	)IQ) where z_r_n < ${end}" +
			"</#if>";
		return wrapSql;
	}

}
