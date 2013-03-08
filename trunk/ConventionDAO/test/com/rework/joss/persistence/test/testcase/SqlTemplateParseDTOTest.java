package com.rework.joss.persistence.test.testcase;

import org.junit.Assert;
import org.junit.Test;

import com.rework.joss.persistence.convention.SqlTemplateParseDTO;

public class SqlTemplateParseDTOTest {
	
	@Test
	public void testNormal() {
		SqlTemplateParseDTO dto = new SqlTemplateParseDTO("select * from test where user=#1# and name=#2#");
		Assert.assertEquals(dto.getParseResult(), "select * from test where user=? and name=?");
		Assert.assertArrayEquals(dto.getTemplateArgs(), new String[]{ "1", "2" });
	}
	
	@Test
	public void testBlankArguments() {
		SqlTemplateParseDTO dto = new SqlTemplateParseDTO("select * from test where user=##");
		Assert.assertEquals(dto.getParseResult(), "select * from test where user=?");
		Assert.assertArrayEquals(dto.getTemplateArgs(), new String[]{ "" });
	}
	
	@Test
	public void testWithSpecialCharater() {
		SqlTemplateParseDTO dto = new SqlTemplateParseDTO("select * from test where user=#\r\n#");
		Assert.assertEquals(dto.getParseResult(), "select * from test where user=?");
		Assert.assertArrayEquals(dto.getTemplateArgs(), new String[]{ "\r\n" });
	}
}
