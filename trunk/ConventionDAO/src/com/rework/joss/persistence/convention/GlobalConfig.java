package com.rework.joss.persistence.convention;

import java.io.IOException;
import java.util.Properties;

public class GlobalConfig {
	
	static Properties p = new Properties();
	
	static{
		try {
			p.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("dao.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String get(String key) {
		return p.getProperty(key);
	}
	
	public static void main(String[] args) {
		System.out.print(GlobalConfig.get("idGenerator"));
	}
}
