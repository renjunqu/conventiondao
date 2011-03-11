package com.rework.joss.persistence.convention.annotation;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;

import com.rework.joss.persistence.convention.BaseDAOByConvention;
import com.rework.joss.persistence.convention.ConventionUtils;
import com.rework.joss.persistence.convention.ORMappingSource;
import com.rework.joss.persistence.convention.id.IdGenerator;
import com.rework.joss.persistence.convention.jdbctype.JdbcTypeHandlerFactory;

/**
 * DAO的抽象类,继承这个类的DAO不需要在spring配置文件中声明
 * 需要在添加类层次的Annotation: @DAO(pojo=DAOTestDTO.class, table="dao_test")
 * 
 * @author kevin zhang
 */
public class AbstractConventionDAO extends BaseDAOByConvention{
	
	@Autowired
	public AbstractConventionDAO() {
		
	}
	
	@Override
	@Autowired
	public void setMetaSource(ORMappingSource metaSource) {
		if(getClass().isAnnotationPresent(DAO.class)){
			DAO dao = getClass().getAnnotation(DAO.class);
			setDbo(dao.table());
			setPojo(dao.pojo().getName());
			if(dao.mapping().length > 0){
				setMapping(ConventionUtils.toMap(dao.mapping()));
			}
		}
		super.setMetaSource(metaSource);
		super.setDataSource(metaSource.getDataSource());
		super.init();
	}
	
	@Override
	@Autowired
	public void setJdbcTypeHandlerFactory(
			JdbcTypeHandlerFactory jdbcTypeHandlerFactory) {
		super.setJdbcTypeHandlerFactory(jdbcTypeHandlerFactory);
	}
	
	@Override
	@Autowired
	public void setGenerator(IdGenerator generator) {
		super.setGenerator(generator);
	}
	
	@Override
	@Resource(name="baseSqlMap")
	public void setBaseSqlMap(Map baseSqlMap) {
		super.setBaseSqlMap(baseSqlMap);
	}

}
