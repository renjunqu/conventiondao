package com.rework.joss.persistence.convention.strategy;

/**
 * 
 * 基于不同数据库的一些特定数据库函数的实现
 * @author kevin zhang
 *
 */
public interface SqlStrategy {

	/**
	 * 对分页sql的包装
	 * @param sqlTemplate
	 */
	String paginate(String sqlTemplate);

}
