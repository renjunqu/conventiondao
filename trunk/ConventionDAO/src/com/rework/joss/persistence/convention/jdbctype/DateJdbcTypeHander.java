package com.rework.joss.persistence.convention.jdbctype;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 日期类型处理类
 * 
 * @author zhujj
 * @create 2007-12-12
 */
public class DateJdbcTypeHander implements JdbcTypeHander {
	
	private static Log log = LogFactory.getLog(DateJdbcTypeHander.class);
	//日期最长长度为19位,格式为：yyyy-MM-dd HH:mm:ss
	private static final int DATE_MAX_LENGTH = 19;
	
	public Object handerJdbcTypeValue(Object value) {
		if(value == null){
			return null;
		}
		log.debug("处理日期类型值：" + value);
		//类型一致则不用处理
		if(value.getClass() == Date.class){
			return value;
		}
		Object resultValue = value;
		String date = String.valueOf(value);
		//如果日期超过了最长的19位
		if(date.length() > DATE_MAX_LENGTH){
			date = date.substring(0, DATE_MAX_LENGTH);
		}
		SimpleDateFormat sf = getDateFormat(date);
		//没有找到合适的时间格式
		if(sf != null){
			try{
				resultValue = sf.parse(date);
			}catch(ParseException ex){
				log.error("解析值["+date+"]为日期类型出错.", ex);
			}
		}
		return resultValue;
		
	}
	
	/**
	 * 根据传入时间的格式返回不同的SimpleDateFormat(yyyyMMdd/yyyy-MM-dd/yyyyMMddHHmi/yyyyMMddHHmiss/yyyy-MM-dd HH:mi/yyyy-MM-dd HH:mi:ss)
	 * @param time
	 * @return
	 */
	public SimpleDateFormat getDateFormat(String time){
		SimpleDateFormat sf = null;
		if (time.length() == 8) {
			sf = new SimpleDateFormat("yyyyMMdd");
		}else if (time.length() == 10) {
			sf = new SimpleDateFormat("yyyy-MM-dd");
		}else if (time.length() == 12) {
			sf = new SimpleDateFormat("yyyyMMddHHmm");
		}else if (time.length() == 14) {
			sf = new SimpleDateFormat("yyyyMMddHHmmss");
		}else if (time.length() == "yyyy-MM-dd HH".length()) {
			sf = new SimpleDateFormat("yyyy-MM-dd HH");			
		}else if (time.length() == 16) {
			sf = new SimpleDateFormat("yyyy-MM-dd HH:mm");			
		}else if (time.length() == 19) {
			sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		return sf;
	}
}
