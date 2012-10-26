package com.rework.joss.persistence.convention;

import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import freemarker.template.Configuration;
import freemarker.template.Template;


public class ExpressionUtil {

	private static Log logger = LogFactory.getLog(ExpressionUtil.class);
	
	public static String parse(String templateStr, Object vObject) {

		Template t;
		StringWriter stringWriter = new StringWriter();
		try {
			t = new Template("heer pagination", new StringReader(templateStr),
					new Configuration());
			
			t.process(vObject, stringWriter);
			stringWriter.toString();
		} catch (Exception e) {
			logger.error("在获取表达式:\n["+templateStr+"]\n时出错!",e);
			return "";
		}
		return stringWriter.toString();
	}

}
