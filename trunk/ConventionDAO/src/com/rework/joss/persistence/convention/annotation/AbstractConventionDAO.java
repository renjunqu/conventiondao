package com.rework.joss.persistence.convention.annotation;

import java.lang.reflect.Field;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.eroi.migrate.Execute;
import com.eroi.migrate.schema.Column;
import com.rework.joss.persistence.convention.BaseDAOByConvention;
import com.rework.joss.persistence.convention.ConventionUtils;
import com.rework.joss.persistence.convention.ORMappingSource;
import com.rework.joss.persistence.convention.db.IJdbcTypeRegistry;
import com.rework.joss.persistence.convention.db.model.ColumnBean;
import com.rework.joss.persistence.convention.db.model.TableBean;
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
            if(StringUtils.isNotBlank(dao.mappingFile())){
                setMappingFilePath( dao.mappingFile() );
            }

		}
		super.setMetaSource(metaSource);
		super.setDataSource(metaSource.getDataSource());
		super.init();
		
		// 要在init后执行
		if(getClass().isAnnotationPresent(DAO.class)){
			DAO dao = getClass().getAnnotation(DAO.class);
			
			TableBean table = metaSource.getTableMetaData(dao.table());
			
            if( dao.pojo().getDeclaredFields().length > 0 ){
            	for (int i = 0; i < dao.pojo().getDeclaredFields().length; i++) {
					Field f = dao.pojo().getDeclaredFields()[i];
					if( f.isAnnotationPresent( DBColumn.class ) ){
						DBColumn column = f.getAnnotation(DBColumn.class);
						ColumnBean columnDatabase = table.getColumn( column.columnName() );
						if( columnDatabase == null ){
							
							// 增加这个字段
							String columnName = column.columnName();
							if( StringUtils.isEmpty( columnName ) ){
								columnName = f.getName();
							}
							Execute.addColumn(this.getConnection(), new Column(
									columnName, 
									column.columnType(), 
									column.length(), 
									column.primaryKey(), 
									column.nullable(), 
									column.defaultValue(), 
									column.autoincrement() 
									)
							, table.getName());
							
							table.notifyColumn(new ColumnBean(
									table, 
									columnName, 
									IJdbcTypeRegistry.getDataType(column.columnType()), 
									column.columnType(), 
									column.length(), 
									column.scale(), 
									column.nullable()?1:0, 
									column.comment())
							);
							
						}
						// 字段是否发生过变化
						else {
							
							Column modify = new Column(
									column.columnName(), 
									column.columnType()
									);
							if( column.length() != columnDatabase.getSize() ){
								modify.setLength( column.length() );
								Execute.alterColumn(this.getConnection()
										, modify
										, table.getName());
							}
							if( column.nullable() != columnDatabase.isNull() ){
								modify.setLength( column.length() );
								Execute.alterColumn(this.getConnection()
										, modify
										, table.getName());
							}
							
						}
					}
				}
            }

		}
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
