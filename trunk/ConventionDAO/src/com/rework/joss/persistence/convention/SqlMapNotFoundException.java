package com.rework.joss.persistence.convention;


/**
 * 在自定义sql配置文件中根据sqlmap key没有找到对应的sql语句时抛出此异常 
 * 
 * @author zhujj
 * @create 2007-11-3
 */
public class SqlMapNotFoundException extends BaseRuntimeException {
	public SqlMapNotFoundException(String sqlmapKey){
		super("["+sqlmapKey+"]在对应的配置文件中没有找到，请在配置文件中增加!");
	}
}
