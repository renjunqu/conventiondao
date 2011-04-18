/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.rework.joss.persistence.test.template;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FmTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws TemplateException 
	 */
	public static void main(String[] args) throws IOException, TemplateException {
		
        Configuration cfg = new Configuration();
		cfg.setClassForTemplateLoading(
				FmTest.class, "/com/heer/gy/persistence/test/template");
		cfg.setObjectWrapper(new DefaultObjectWrapper());

        /* ------------------------------------------------------------------- */    
        /* You usually do these for many times in the application life-cycle:  */    
                
        /* Get or create a template */
        Template temp = cfg.getTemplate("fm_macro_test.ftl");

        /* Create a data model */
        Map root = new HashMap();
        root.put("property", "Big Joe");
        root.put("test", "test result");

        /* Merge data model with template */
        Writer out = new OutputStreamWriter(System.out);
        temp.process(root, out);
        out.flush();

	}

}
