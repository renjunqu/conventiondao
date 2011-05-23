package com.rework.joss.persistence.convention;
 
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.rework.core.dto.BaseObject;

/**
 * sql模版解析DTO,用来解析类似于下面的语句：
 * <p>
 *     (1) select * from xxx where xx=#v1# and yy=#v2#
 * <p>
 * 此类用来生成上面sql的预定义sql语句：
 * <p>
 * 	   (2) select * from xxx where xx=? and yy=?
 * <p>
 * 并且解析获得(1)中的##之间的值
 * @author zhujj
 * @create 2007-11-6
 */
public class SqlTemplateParseDTO extends BaseObject {
	private Log logger = LogFactory.getLog(ConventionUtils.class);
	
	private String template;
	private String templateAfterParse;
	private Object[] args = null;
	
	public SqlTemplateParseDTO(String template){
		super();
		this.template = template;
		parse();
	}
	
	/**
	 * 解析类似于下面的语句：<p>
	 * 		select * from xxx where xx=#v1# and yy=#v2#<p>
	 * 生成sql的预定义sql语句：<p>
	 * 		select * from xxx where xx=? and yy=?
	 */
	private void parse(){
		if(StringUtils.isBlank(template))
			throw new RuntimeException("没有预先配置需要解析的sql语句！");
		String[] tokens = StringUtils.split(template,"#");
		if(tokens != null && tokens.length > 0){
			StringBuffer parseSql = new StringBuffer();
			List argsList = new LinkedList();
			for(int i = 0; i < tokens.length; i ++){
				// 匹配 #任意字符# ,然后替换为?
				if(i%2 == 1){
					parseSql.append("?");
					argsList.add(tokens[i]);
				}else{
					parseSql.append(tokens[i]);
				}
			}
			templateAfterParse = parseSql.toString();
			args = argsList.toArray();
		}else{
			templateAfterParse = template;
		}
	}
	/**
	 * 解析类似于下面的语句：<p>
	 * 		select * from xxx where xx=#v1# and yy=#v2#<p>
	 * 生成sql的预定义sql语句：<p>
	 * 		select * from xxx where xx=? and yy=?
	 */
	public String getParseResult(){
		if(logger.isDebugEnabled()){
			// logger.debug("解析前:\n ["+template+"]");
			logger.debug("解析后:\n ["+templateAfterParse+"]");
		}
		return templateAfterParse;
	}
	
	/**
	 * 解析获得(select * from xxx where xx=#v1# and yy=#v2#)中的##之间的值
	 * @return
	 */
	public Object[] getTemplateArgs(){
		if(logger.isDebugEnabled()){
			if(args == null)
				logger.info("解析后:\n 参数列表[]");
			else
				logger.info("解析后:\n 参数列表["+StringUtils.join(args, ",")+"]");
		}
		return args;
	}
}
