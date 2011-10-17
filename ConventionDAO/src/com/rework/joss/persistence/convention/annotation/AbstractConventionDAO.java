package com.rework.joss.persistence.convention.annotation;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceUtils;

import com.eroi.migrate.Execute;
import com.eroi.migrate.schema.Column;
import com.eroi.migrate.schema.Table;
import com.rework.joss.persistence.convention.BaseDAOByConvention;
import com.rework.joss.persistence.convention.BaseRuntimeException;
import com.rework.joss.persistence.convention.ConventionUtils;
import com.rework.joss.persistence.convention.ORMappingSource;
import com.rework.joss.persistence.convention.db.DBFactory;
import com.rework.joss.persistence.convention.db.IDBFacade;
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
		
		
		if(!getClass().isAnnotationPresent(DAO.class)){
			throw new BaseRuntimeException("no DAO annotation defined!");
		}
		
		boolean isModified = false;
		
		String tableName = null;
		List columns = new ArrayList();
		DAO dao = getClass().getAnnotation(DAO.class);
		setDbo(dao.table());
		setPojo(dao.pojo().getName());
		if(dao.mapping().length > 0){
			setMapping(ConventionUtils.toMap(dao.mapping()));
		}
        if(StringUtils.isNotBlank(dao.mappingFile())){
            setMappingFilePath( dao.mappingFile() );
        }
        
        
        
        super.initStrategy();
		super.setMetaSource(metaSource);
		super.setDataSource(metaSource.getDataSource());
		
        // 判断这个表是否存在
		/*
        try {
			ResultSet rs = getConnection().getMetaData().getTables(null, null, dao.table(), new String[]{ "TABLE", "VIEW" });
			if( rs.next() ){
				// create table
				tableName = dao.table();
				logger.debug(tableName + "已经存在！");
			}
			rs.close();
		} catch (Exception e) {
			throw new BaseRuntimeException(e.getMessage());
		} finally {
			DataSourceUtils.releaseConnection(getConnection(), getDataSource());
		}
		*/
		// 在预加载的模式下，使用这种方式判断
		if( metaSource.isTableExist(dao.table()) ){
			tableName = dao.table();
		}
        
        if( tableName != null ){
        	super.init();
        	if( dao.pojo().getDeclaredFields().length > 0 ){
            	for (int i = 0; i < dao.pojo().getDeclaredFields().length; i++) {
					Field f = dao.pojo().getDeclaredFields()[i];
					if( f.isAnnotationPresent( DBColumn.class ) ){
						DBColumn column = f.getAnnotation(DBColumn.class);
						
						// 增加这个字段
						String columnName = column.columnName();
						if( StringUtils.isEmpty( columnName ) ){
							columnName = conventionStrategy.translateFromPropertyToColumn( f.getName() );
						}
						TableBean table = metaSource.getTableMetaData( tableName );
						ColumnBean columnDatabase = table.getColumn( columnName );
						if( columnDatabase != null ){
							/*
							Column modify = new Column(
									columnName, 
									column.columnType(), 
									column.length(), 
									column.primaryKey(), 
									column.nullable(), 
									column.defaultValue(), 
									column.autoincrement() 
									);
							*/
							Column modify = new Column(
									columnName, 
									column.columnType()
									);
							if( column.length() != columnDatabase.getSize() ){
								modify.setLength( column.length() );
								isModified = true;
							}
							if( column.nullable() != columnDatabase.isNull() ){
								modify.setNullable( column.nullable() );
								isModified = true;
							}
							if( column.autoincrement() != columnDatabase.isAutoincrement() ){
								modify.setAutoIncrement( true );
								isModified = true;
							}
							
							if( column.primaryKey() != columnDatabase.isPrimaryKey() ){
								if( column.primaryKey() ){
									modify.setPrimaryKey(true);
								}else{
									Execute.dropColumn(columnName, table.getName());
								}
								
								isModified = true;
							}
							if( isModified ){
								try {
									Execute.alterColumn(this.getConnection()
										, modify
										, table.getName());	
								}finally {
									DataSourceUtils.releaseConnection(getConnection(), getDataSource());
								}
							}
						}else{
							try {
								Execute.addColumn(getConnection(), new Column(
									columnName, 
									column.columnType(), 
									column.length(), 
									column.primaryKey(), 
									column.nullable(), 
									column.defaultValue(), 
									column.autoincrement() 
									), tableName);
								isModified = true;
							}finally {
								DataSourceUtils.releaseConnection(getConnection(), getDataSource());
							}
						}
					}
				}
        	}
        	
        }else{
        	tableName = dao.table();
            for (int i = 0; i < dao.pojo().getDeclaredFields().length; i++) {
				Field f = dao.pojo().getDeclaredFields()[i];
				if( f.isAnnotationPresent( DBColumn.class ) ){
					DBColumn column = f.getAnnotation(DBColumn.class);
					
					// 增加这个字段
					String columnName = column.columnName();
					if( StringUtils.isEmpty( columnName ) ){
						columnName = conventionStrategy.translateFromPropertyToColumn( f.getName() );
					}
					TableBean table = metaSource.getTableMetaData( dao.table() );
					ColumnBean columnDatabase = table.getColumn( columnName );
					if( columnDatabase == null ){
						columns.add(new Column(
								columnName, 
								column.columnType(), 
								column.length(), 
								column.primaryKey(), 
								column.nullable(), 
								column.defaultValue(), 
								column.autoincrement() 
								));
					}
				}
			}
            if( columns.size() > 0 ){
            	try{
            	Execute.createTable(getConnection(), new Table(tableName, (Column[]) columns.toArray(new Column[0])));
            	}finally {
					DataSourceUtils.releaseConnection(getConnection(), getDataSource());
				}
            	isModified = true;
            }
        }
        // 如果新加了表，或者修改了字段 
		if( isModified ){
			// 重新加载
			super.init();
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
