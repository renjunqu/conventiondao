package com.rework.joss.persistence.convention.id;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.FactoryBean;

import com.rework.joss.persistence.convention.GlobalConfig;


/**
 * 得到id生成策略
 * @author kevin zhang
 *
 */
public class IDGeneratorFactory implements FactoryBean{

	/**
	 * 得到id生成策略
	 * 可能为空, 为空的时候id通过数据库机制生成
	 * @return
	 */
	public Object getObject() throws Exception {
		String idGenerator = GlobalConfig.get("idGenerator");
		if(StringUtils.isNotBlank(idGenerator)){
			if("uuid".equals(idGenerator)){
				return new UUIDIdGenerator();
			}else if("uuid".equals(idGenerator)){
				
			}
		}
		return null;
	}

	public Class getObjectType() {
		return IdGenerator.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
