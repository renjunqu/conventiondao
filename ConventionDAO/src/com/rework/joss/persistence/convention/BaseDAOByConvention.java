/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.rework.joss.persistence.convention;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.rework.core.dto.BaseObject;
import com.rework.joss.persistence.IBaseDAO;
import com.rework.joss.persistence.convention.annotation.DAO;
import com.rework.joss.persistence.convention.annotation.Fetch;
import com.rework.joss.persistence.convention.db.Constants;
import com.rework.joss.persistence.convention.db.DBFactory;
import com.rework.joss.persistence.convention.db.IJdbcTypeRegistry;
import com.rework.joss.persistence.convention.db.model.ColumnBean;
import com.rework.joss.persistence.convention.db.model.RuntimeRowObject;
import com.rework.joss.persistence.convention.db.model.TableBean;
import com.rework.joss.persistence.convention.id.IdGenerator;
import com.rework.joss.persistence.convention.jdbctype.JdbcTypeHandlerFactory;
import com.rework.joss.persistence.convention.strategy.SqlStrategyFactory;
import com.rework.joss.persistence.convention.type.TypeHandler;
import com.rework.joss.persistence.convention.type.TypeHandlerFactory;
import com.rework.utils.UtilMisc;

import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.Template;

/**
 * 约定
 * 荐于数据库标准中规定
 * <li>数据库和pojo的字段是采用 camel case的对应关系,
 * <li>使用逻辑主键,并且数据库中使用 表名去掉前缀加+_ID 对应 pojo中的id
 * 
 * 因此大部分的表操作的sql可以直接生成
 * 
 * 由于这个约定的存在我们可以大量的简化对数据库的操作的代码的编写
 * 
 * 依整的信息
 * 表名
 * 
 * 特殊情况的处理
 * 字段不能完全对应(目前来看某种程度上现在的id就是一个特殊情况)
 * 
 * 需要一个公共的仓库来缓存已经加载的信息
 * 
 * 需要一结构来存储字段和属性的对应关系
 * tableObject在init()执行后存放大部分的数据库和pojo的映射信息
 * 
 * 
 * 
 * @author zhangsh
 *
 */
public class BaseDAOByConvention extends JdbcDaoSupport implements IBaseDAO {

	private static final String REMOVE = "remove";

	private static final String UPDATE_KEY = "update";

	private static final String KEY_EMPTY = "Empty";

	private static final String AND = " and ";

	private static final String ORDER_BY = "order by ";

	protected static Log logger = LogFactory.getLog(BaseDAOByConvention.class);
	
	//~ spring injection
	/**
	 * 数据库对象名(表,视图)
	 * database object name 
	 */
	private String dbo;
	
	/**
	 * java值对象(有的也叫vo)
	 * plain old java object
	 */
	private String pojo;

    private String mappingFilePath;
    
    private Map<String, String> userSqlMap = null;
    
    private Long lastModifyTime = null;

	private ORMappingSource metaSource;
	
	/**
	 * 本身定义的sqlmap
	 */
	private Map baseSqlMap = new HashMap();
	
	/**
	 * 子类自定义的sqlmap
	 */
	private Map sqlMap = new HashMap();
	
	/**
	 * 字段对应关系
	 */
	private Map colMap = new HashMap();
	
	/**
	 * 缓存java值对象类型,这里不采用对象实例缓存，因为可能同时调用不同方法的时候可能会造成冲突
	 */
	private Class pojoClass;
	
	/**
	 * 类型转换Factory,从JDBC type到java type(从ibatis移植)
	 */
	TypeHandlerFactory typeHandlerFactory = new TypeHandlerFactory();
	
	/**
	 * 将pojo中的值转换成jdbc type的值
	 */
	private JdbcTypeHandlerFactory jdbcTypeHandlerFactory;
	
	/**
	 * 这个dao对应的pojo和row的转换策略
	 */
	protected IConventionStrategy conventionStrategy;
	
	/**
	 * 数据模型关系,在dao加载时进行初始化 
	 */
	private TableBean tableObject;
	
	/**
	 * 得到id生成策略
	 */
	private IdGenerator generator;
	
	protected void initStrategy(){
		if(logger.isDebugEnabled()){
			logger.debug("init BaseDAO, 数据表: [" + dbo + "], 对应java 对象["+ pojo +"]");
		}
		String conventionStrategyName = GlobalConfig.get("conventionStrategy");
		if(StringUtils.isNotBlank(conventionStrategyName)){
			try {
				conventionStrategy = (IConventionStrategy) ClassUtils.forName(conventionStrategyName, getClass().getClassLoader()).newInstance();
			} catch (Exception e) {
				logger.error("初始化 IConventionStrategy 出错：" + conventionStrategyName, e);
				logger.info("使用默认的 DefaultConventionStrategy");
				conventionStrategy =  new DefaultConventionStrategy();
			}
		}else{
			conventionStrategy =  new DefaultConventionStrategy();
			logger.info("使用默认的 DefaultConventionStrategy");
		}
		 
		conventionStrategy = new ConvetionStrategyApplyColMap(conventionStrategy, colMap);
	}
	
	/**
	 * 初始化方法
	 * 通过spring执行
	 */
	public void init(){
		tableObject = metaSource.getTableMetaData( dbo);
	}
	
	public void setDbo(String dbo) {
		this.dbo = dbo;
	}

	public void setPojo(String pojo) {
		this.pojo = pojo;
	}

	public void setMetaSource(ORMappingSource metaSource) {
		this.metaSource = metaSource;
	}
	
	public void setBaseSqlMap(Map baseSqlMap) {
		this.baseSqlMap = baseSqlMap;
	}

	public void setSqlMap(Map sqlMap) {
		this.sqlMap = sqlMap;
	}
	
	// just call once
	public void setColMap(Map colMap) {
		this.colMap = colMap;
	}

    public void setMappingFilePath(String path){
        this.mappingFilePath = path;
    }

	/**
	 * 同上, not init
	 * @param colMap
	 */
	public void setMapping(Map colMap) {
		this.colMap = colMap;
	}
	
	public void setJdbcTypeHandlerFactory(
			JdbcTypeHandlerFactory jdbcTypeHandlerFactory) {
		this.jdbcTypeHandlerFactory = jdbcTypeHandlerFactory;
	}
	
	public void setGenerator(IdGenerator generator) {
		this.generator = generator;
	}
	//~ spring injection end
	
	






	/**
	 * 数据库结构dbo与pojo的映射关系
	 */
	private RowMapper currentrowmapper;
	
	private RowMapper getRowMapper(){
		if(null == currentrowmapper){
			 currentrowmapper = initRowMapper(tableObject, pojo);
		}
		return currentrowmapper;
	}
		
		
	
/*	
	private RowMapper joinRowMapper = new RowMapper(){
		public Object mapRow(ResultSet rs, int num) throws SQLException {
			
			Map returnMap = new HashMap();
			
			Object object = getBaseObjectByPojoPath();
			BaseObject childObject = null;
			try {
				Class childObjectClass = ClassUtils.forName( join.getPojoClass() );
				childObject = (BaseObject) childObjectClass.newInstance();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			
			ResultSetMetaData metaData = rs.getMetaData();
			int count = metaData.getColumnCount();
			if(logger.isTraceEnabled()){
				logger.info("start to copy property........");
			}
			Set parentColumnsSet = new HashSet();
			Map listGroupByPKValue = new HashMap();
			boolean isSubClass = false;
			// 子表对象是否有值
			boolean isChildObjectNull = true;
			for (int i = 0; i < count; i++) {
				String colName = metaData.getColumnName(i+1);
				ColumnBean column = tableObject.getPojoColumnByColumnName(colName);
				ColumnBean childColumn = join.getTableBean().getPojoColumnByColumnName(colName);
				
				// 如果是父表的主键
				if( colName.equals( tableObject.getPkColumn().getName() ) && !parentColumnsSet.contains(column) ){
					returnMap.put("pkvalue", rs.getObject( colName ));
				}
				// 父表和子表和分隔字段
				if( "testtesttestdivide".equals( colName ) && !isSubClass ){
					isSubClass = true;
				}
				// 判断规则, 先可着父类
				if(!isSubClass){
					if( null != column && !parentColumnsSet.contains(column) ){
						String propName = ConventionUtils.getPropName(conventionStrategy, column.getName(), column.isPrimaryKey());
						try {
							setPropertyValue(object, propName, rs, colName, column.getJdbcType());
						} catch (Exception e) {
							logger.error("copy[" + colName + "->property" + propName + "[error：" + e.getMessage(), e);
						}
						parentColumnsSet.add( column );
					}
				}else{
					if(null != childColumn){
						String propName = ConventionUtils.getPropName(conventionStrategy, childColumn.getName(), childColumn.isPrimaryKey());
						try {
							setPropertyValue(childObject, propName, rs, colName, childColumn.getJdbcType());
						} catch (Exception e) {
							logger.error("copy[" + colName + "->property" + propName + "[error：" + e.getMessage(), e);
						}
					}
				}
			}
			returnMap.put("parentObject", object);
			returnMap.put("childObject", childObject);
			if(logger.isTraceEnabled()){
				logger.info("end copy property........");
			}
			return returnMap;
		}
	};
	
*/
	
	private void setPropertyValue(Object targetObject, 
			String propName,  
			ResultSet rs, 
			String colName,
			int jdbcType)
	throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, SQLException{
		if(!PropertyUtils.isWriteable(targetObject, propName)){
			return;
		}
		//得到属性的class类型
		Class propertyType = PropertyUtils.getPropertyDescriptor(targetObject, propName).getPropertyType();
		// 将数据库中得到的数据转换成属性的数据(使用typeHandlerFactory进行类型转换)
		
		TypeHandler th = typeHandlerFactory
			.getTypeHandler( propertyType, IJdbcTypeRegistry.getDataType(jdbcType) );
		Object propValue = th.getResult( rs, colName.toUpperCase() );
		PropertyUtils.setProperty(targetObject, propName, propValue);
		if(logger.isTraceEnabled()){
			logger.info("copy["+colName+"->"+propName+"["+propValue+"]");
		}
	}
	
	private RowMapper initRowMapper(final TableBean tableBean, final String pojoClassName) {
		return new RowMapper(){
			public Object mapRow(ResultSet rs, int num) throws SQLException {
				
				Object object = null;
				Class pojoClass = null;
				try {
						pojoClass = ClassUtils.forName(pojoClassName);
						object = pojoClass.newInstance();
				} catch (Exception e) {
					throw new RuntimeException("实例化[" + pojoClassName
							+ "]出错！注意：pojo必须继承BaseObject.", e);
				}
				
				ResultSetMetaData metaData = rs.getMetaData();
				int count = metaData.getColumnCount();
				if(logger.isTraceEnabled()){
					logger.info("start to copy property........");
				}
				for (int i = 0; i < count; i++) {
					String colName = metaData.getColumnLabel(i+1);
					ColumnBean column = tableBean.getPojoColumnByColumnName(colName);
					if(null == column) {
						//不是自己对应表的字段
						column = new ColumnBean();
						column.setName(colName);
						column.setJdbcType(metaData.getColumnType(i + 1));
					}
					String propName = ConventionUtils.getPropName(conventionStrategy, column.getName(), column.isPrimaryKey());
					try {
						setPropertyValue(object, propName, rs, colName, column.getJdbcType());
					} catch (Exception e) {
						logger.error("copy[" + colName + "->property" + propName + "[error：" + e.getMessage());
					} 
				}
				if(logger.isTraceEnabled()){
					logger.info("end copy property........");
				}
				return object;
			}
		};
	}

	/**
	 * 通过pojo类型返回对应的实例
	 * pojoClass pojo对应的实例，如果pojoClass不为空则返回这个pojoClass对应的类型
	 * pojo的类路径,如果pojoClass为null的话则实例话
	 * @return
	 */
	public BaseObject getBaseObjectByPojoPath() {
		try {
			if (pojoClass != null) {
				return (BaseObject) pojoClass.newInstance();
			} else {
				pojoClass = ClassUtils.forName(pojo);
				return (BaseObject) pojoClass.newInstance();
			}
		} catch (Exception e) {
			throw new RuntimeException("实例化[" + pojo
					+ "]出错！注意：pojo必须继承BaseObject.", e);
		}
	}
	
	/**
	 * 初化执行用sql
	 * @param sqlTemplate
	 * @param paramMap 用于构造sql的参数map,外部传入,和tableobject进行
	 * @return
	 */
	private String initSqlMapByKey(String sqlTemplate, RuntimeRowObject rowObject, Object paramMap) {
		Map tempMap = new HashMap();
		// TODO for check
		if(paramMap == null){
		}else if(paramMap instanceof Map){
			tempMap = (Map) paramMap;
		}else{
			tempMap.putAll( new BeanMap(paramMap) );
		}
		// tbl是关键字,不能重复
		if(null != tempMap.get("tbl")){
			throw new RuntimeException("tbl是程序已经用到的关键字,请使用其它关键字!");
		}
		tempMap.put("tbl", rowObject);
		
		String sqlMap = parse(sqlTemplate, tempMap);
		if(logger.isDebugEnabled()){
			logger.info(sqlMap);
		}
		return sqlMap;
	}

	private String getSqlTemplate(String sqlMapKey) {
		String sql = (String) this.baseSqlMap.get(sqlMapKey);
		if(StringUtils.isBlank(sql)){
			throw new SqlMapNotFoundException(sqlMapKey);
		}
		return sql;
	}
	
	/**
	 * 把pojo对象和数据库结构结合起来做成一个信息最完整的对象,用于sqlmap的解析
	 * @param obj
	 * @return
	 */
	private RuntimeRowObject mixDbAndPojo(Object obj) {
		if(obj == null){
			obj = getBaseObjectByPojoPath();
		}
		return new RuntimeRowObject(tableObject, obj, null, this.conventionStrategy);
	}	
	
	/**
	 * 判断某个属性值是否在数据库中存在对应的字段
	 * @param propertyName
	 * @return
	 */
	private boolean isPropertyHasMappingWithColumn(String propertyName){
		String columnName = conventionStrategy.translateFromPropertyToColumn(propertyName);
		//判断是否是自定义字段映射 
		if(colMap.containsKey(columnName)){
			return true;
		}
		//判断位于数据库结构中
		if(tableObject.getColumn(columnName) != null){
			return true;
		}
		return false;
	}
	
	public BaseObject findById(Object id){
		return findById(id, new HashMap());
	}
	
	public BaseObject findById(Object id, Map paramMap) {
		Assert.notNull(id, "id不能为空");
		if(tableObject.getPkColumns().isEmpty()){
			throw new BaseRuntimeException("未定义主键!");
			/*
			if(!isPropertyHasMappingWithColumn("id")){
				throw new RuntimeException("属性中的id没有与数据库字段进行对应!");
			}
			BaseObject queryDTO = getBaseObjectByPojoPath();
			queryDTO.setId(id);
			List resultList = query(queryDTO, null);
			return resultList.size() > 0 ? (BaseObject)resultList.get(0) : null;
			*/
		}else{
			String sqlTemplate = getSqlTemplate("findById");
			String sql = initSqlMapByKey(sqlTemplate, mixDbAndPojo(null), null);
			try{
				BaseObject result = (BaseObject)getJdbcTemplate().queryForObject(sql,new Object[]{id},getRowMapper());
				processFetchProperties(paramMap, sql, new Object[]{id}, ConventionUtils.list(result));
				return result;
			}catch(EmptyResultDataAccessException emptyEx){
				return null;
			}
		}
	}
	
	public List findByIds(Object[] ids) {
		Assert.notEmpty(ids, "不能存在空id");
		if(tableObject.getPkColumns().isEmpty()){
			throw new BaseRuntimeException("未定义主键!");
		}else{
			String sqlTemplate = getSqlTemplate("findByIds");
			
			String sql = initSqlMapByKey( sqlTemplate, mixDbAndPojo(null), ConventionUtils.toMap("ids", ids) );
			try{
				List result = getJdbcTemplate().query(sql, ids, getRowMapper());
				processFetchProperties(new HashMap(), sql, ids, ConventionUtils.list(result));
				return result;
			}catch(EmptyResultDataAccessException emptyEx){
				return null;
			}
		}
	}
	
	/*public BaseObject findById(String id) {
		Assert.hasText("id", "id不能为空");
		String sql = initSqlMap("findById", mixDbAndPojo(null), null);
		try{
			return (BaseObject)getJdbcTemplate().queryForObject(sql,new Object[]{id},mapper);
		}catch(EmptyResultDataAccessExcekption emptyEx){
			return null;
		}
	}*/
	/**
	 * 执行增/删/改等操作
	 * @param sqlMapKey
	 * @param dto 值对象或者Map
	 * @param isEmpty 如果为true则参数中忽略null和空字符，如果为false则参数中只忽略null
	 * @return 更新记录数
	 */
	private int excuteUpdate(String sqlMapKey, Object dto, boolean isEmpty){
		
		RuntimeRowObject row = mixDbAndPojo(dto);
		for (Iterator iterator = row.getColumnsWithNotEmptyValue().iterator(); iterator.hasNext();) {
			ColumnBean cbo = (ColumnBean) iterator.next();
			logger.debug( cbo.getName() + ":" + cbo.getPropValue() );
		}
		String sql = getSqlTemplate(sqlMapKey);
		String prepareStatSql = initSqlMapByKey(sql, row, null);
		SqlArgTypeSetter argTypeSetter = null;
		if(isEmpty)
			argTypeSetter = ConventionUtils.getPropertyValuesIgnoreEmpty(dto, this.tableObject, jdbcTypeHandlerFactory, this.conventionStrategy);
		else
			argTypeSetter = ConventionUtils.getPropertyValuesIgnoreNull(dto, this.tableObject, jdbcTypeHandlerFactory, this.conventionStrategy);
		Object[] args = argTypeSetter.getArgs();
		int[] argTypes = argTypeSetter.getArgTypes();
		if(logger.isDebugEnabled()){
			String debugMsg = "ConventionDAO arguments : ";
			for (int i = 0; i < args.length; i++) {
				debugMsg += "," + i + ":[" + args[i] + "]";
			}
			logger.debug( debugMsg );
		}
		
		int optNum = 0;
		if(isExistsClobColumn(argTypes)){
			//clob型需要特殊处理
			optNum = getJdbcTemplate().update(prepareStatSql, args, argTypes);
		}else{
			optNum = getJdbcTemplate().update(prepareStatSql, args);
		}
		if(logger.isDebugEnabled())
			logger.info("完成对"+optNum+"条记录的操作！");
		return optNum;
	}
	
	/**
	 * 处理字段中是否存在clob类型
	 * @param argTypes
	 * @return
	 */
	private boolean isExistsClobColumn(Integer[] argTypes) {
		return ArrayUtils.indexOf(argTypes, Types.CLOB) >= 0;
	}
	
	private boolean isExistsClobColumn(int[] argTypes) {
		return ArrayUtils.indexOf(argTypes, Types.CLOB) >= 0;
	}
	
	/**
	 * 执行查询操作, 用预定义的key, 
	 * @param sqlMapKey
	 * @param dto
	 * @param paramMap
	 * @return
	 */
	private List excuteQueryUsingBaseObject(String sqlMapKey, BaseObject dto, Map paramMap) {
		String sqlTemplate = getSqlTemplate(sqlMapKey);
		// 分页支持
		sqlTemplate = SqlStrategyFactory.getBean(getDataSource()).paginate(sqlTemplate);
		return excuteQueryByTpl(sqlTemplate, dto, paramMap);
	}
	
	public List excuteQueryByTpl(String sqlTemplate, BaseObject dto, Map paramMap) {
		RuntimeRowObject rowObject = mixDbAndPojo(dto);
		String prepareStatSql = initSqlMapByKey(sqlTemplate, rowObject, paramMap);
		Object[] args = ConventionUtils.getPropertyValuesIgnoreEmpty(
				dto,
				this.tableObject, 
				jdbcTypeHandlerFactory, this.conventionStrategy).getArgs();
		if (logger.isDebugEnabled())
			logger.info("args[" + StringUtils.join(args, ",") + "]");
		String fuzzyType = (String) paramMap.get(PARAM_QUERY_FUZZY_TYPE);
		// 对通用模糊查询的处理
		if(StringUtils.isNotBlank(fuzzyType)){
			args = fuzzyArgs(args, fuzzyType);
		}
		
		List list = getJdbcTemplate().query(prepareStatSql, args, getRowMapper());
		processFetchProperties(paramMap, prepareStatSql, args, list);
		return list;
	}

	/**
	 * 查询到子表结果并写入到对象中
	 * @param prepareStatSql
	 * @param args
	 * @param resultList
	 * @param join
	 */
	private void processJoinResult(String prepareStatSql, Object[] args, List resultList, JoinTable join) {
		TableBean childTableBean = metaSource.getTableMetaData( join.getTableName() );
		List childList = getJdbcTemplate().query( join.wrapSql( prepareStatSql ), args, initRowMapper( childTableBean, join.getPojoClass() ) );
		
		for (Iterator iterator = resultList.iterator(); iterator.hasNext();) {
			Object parent = (Object) iterator.next();
			// 得到parent的主键值
			Object pkvalue = getPkValue(parent);
			List childTarget = new ArrayList();
			// 判断child的外键值是匹配parent的主键值,从而进行关联
			for (Iterator iterator2 = childList.iterator(); iterator2.hasNext();) {
				BaseObject child = (BaseObject) iterator2.next();
				Object fkvalue = getColumnValue(child, join.getJoinColumnName(), false);

				if( fkvalue.toString().equals( pkvalue.toString() ) ){
					childTarget.add(child);
				}
			}
			try {
				PropertyUtils.setProperty(parent, join.getTargetProperty(), childTarget);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
	}
	
	/**
	 * 用于支持模糊查询,
	 * 模糊类型, 包括 *xxx, xxx*, *xxx*
	 * @param args
	 * @param fuzzyType
	 * @return
	 */
	private String[] fuzzyArgs(Object[] args, String fuzzyType) {
		String[] values = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			if(QuerySetting.FUZZY_TYPE_BEFORE.equals(fuzzyType)){
				values[i] = "%" + args[i].toString();
			}else if(QuerySetting.FUZZY_TYPE_AFTER.equals(fuzzyType)){
				values[i] = args[i].toString() + "%";
			}else if(QuerySetting.FUZZY_TYPE_BOTH.equals(fuzzyType)){
				values[i] = "%" + args[i].toString() + "%";
			}else{
				throw new IllegalArgumentException("不正确的模糊查询类型:"+fuzzyType);
			}
		}
		if (logger.isDebugEnabled())
			logger.info("fuzzy args[" + StringUtils.join(values, ",") + "]");
		return values;
	}
	
	/**
	 * 执行查询总数操作
	 * @param sqlMapKey
	 * @param dto
	 * @return
	 */
	private Integer excuteQueryCount(String sqlMapKey, Object param){
		RuntimeRowObject rowObject = mixDbAndPojo(param);
		String sql = getSqlTemplate(sqlMapKey);
		String prepareStatSql = initSqlMapByKey(sql, rowObject, param);
		
		Object[] args = ConventionUtils.getPropertyValuesIgnoreEmpty(param, this.tableObject, jdbcTypeHandlerFactory, this.conventionStrategy).getArgs();
		if(logger.isDebugEnabled())
			logger.info("args["+StringUtils.join(args,",")+"]");
		String fuzzyType = null;
		try {
			fuzzyType = (String)BeanUtils.getProperty(param, PARAM_QUERY_FUZZY_TYPE);
		} catch (Exception e) {
			logger.info("get fuzzyType from object error");
		} 
		// 对通用模糊查询的处理
		if(StringUtils.isNotBlank(fuzzyType)){
			String[] values = fuzzyArgs(args, fuzzyType);
			long resultSize = getJdbcTemplate().queryForLong(prepareStatSql, values);
			return NumberUtils.createInteger(String.valueOf(resultSize)) ;
		}else{
			long resultSize = getJdbcTemplate().queryForLong(prepareStatSql, args);
			return NumberUtils.createInteger(String.valueOf(resultSize)) ;
		}
		
	}
	
	
	public void create(BaseObject dto) {
		Object pkValue = null;
		try{
			pkValue = getPkValue(dto);
		}catch(Exception ex){
			//ignore
		}
		if(pkValue == null || StringUtils.isEmpty(pkValue.toString())) {
			if(null != generator){
				setPkValue(dto, generator.id()); 
			}
		}
		excuteUpdate("create", dto, false);
	}

	/**
	 * 得到dto中主键的值
	 * to be move to {@link ConventionUtils}
	 * @param dto
	 * @param value
	 */
	private void setPkValue(BaseObject dto, Object value) {
		if( null == tableObject.getPkColumn() ){
			throw new BaseRuntimeException("需要为表定义主键！");
		}
		String pkColumnName = tableObject.getPkColumn().getName();
		// 主键对应的java属性名 
		String javaPropName = conventionStrategy.translateFromColumnToProperty(pkColumnName, true);
		try {
			BeanUtils.setProperty(dto, javaPropName, value);
		} catch (Exception e) {
			throw new BaseRuntimeException("给主键赋值出错!", e);
		}
	}
	
	/**
	 * 得到主键的值
	 * TODO 用threadlocal可以提高一点点效率
	 * to be move to {@link ConventionUtils}
	 * @param dto
	 * @return
	 */
	private Object getPkValue(Object dto) {
		String pkColumnName = tableObject.getPkColumn().getName();
		return getColumnValue(dto, pkColumnName, true);
	}

	private Object getColumnValue(Object pojo, String columnName, boolean isPrimaryKey) {
		// 主键对应的java属性名 
		String javaPropName = conventionStrategy.translateFromColumnToProperty(columnName, isPrimaryKey);
		try {
			return BeanUtils.getProperty(pojo, javaPropName);
		} catch (Exception e) {
			throw new BaseRuntimeException("给主键赋值出错!", e);
		}
	}
	
	public void create(BaseObject dto, String id) {
		Assert.notNull(dto);
		setPkValue(dto, id);
		create(dto);
	}

	public void create(BaseObject[] dtos) {
		if(dtos == null || dtos.length < 1)
			return;
		
		for(int i = 0; i < dtos.length; i++){
			Object pkValue = null;
			try{
				pkValue = getPkValue(dtos[i]);
			}catch(Exception ex){
				//ignore
			}
			if(pkValue == null || StringUtils.isEmpty(pkValue.toString())) {
				if(null != generator){
					setPkValue(dtos[i], generator.id()); 
				}
			}
		}
		for(int i = 0; i < dtos.length; i++){
			create(dtos[i]);
		}
		/**
		// mysql的情况下,有更简便高效的处理方法,下面优化仅对所有创建对象不为null的属性都一致的情况，如果不同对象对应的非空字段不同的话这里处理会有问题，先好使再说，待优化 TODO commented by zhujj
		String type = DBFactory.getDBType(this.getDataSource());
		if(Constants.MYSQL.equals(type)){
		
			RuntimeRowObject row = mixDbAndPojo(dtos[0]);
			String sql = getSqlTemplate("createsForMysql");
			String prepareStatSql = initSqlMapByKey(sql, row, UtilMisc.toMap("valuescount", dtos.length ));
			
			Object[] argsArray = new Object[]{};
			int[] intArgTypes = new int[]{};
			for(int i = 0; i < dtos.length; i++){
				SqlArgTypeSetter argTypeSetter = ConventionUtils.getPropertyValuesIgnoreNull(dtos[i], this.tableObject, jdbcTypeHandlerFactory, this.conventionStrategy);
				if(logger.isDebugEnabled())
					logger.info("args["+StringUtils.join(argTypeSetter.getArgs(),",")+"]");
				
				argsArray = ArrayUtils.addAll( argsArray , argTypeSetter.getArgs() );
				intArgTypes = ArrayUtils.addAll( intArgTypes , argTypeSetter.getArgTypes() );
			}
			
			if(isExistsClobColumn( intArgTypes )){
				//clob型需要特殊处理
				getJdbcTemplate().update(prepareStatSql, argsArray, intArgTypes);
			}else{
				getJdbcTemplate().update(prepareStatSql, argsArray);
			}
			
		}else{
			for(int i = 0; i < dtos.length; i++){
				create(dtos[i]);
			}
		}*/
	}
	
	public String createAndId(BaseObject dto) {
		Assert.notNull(dto);
		//dto.setId(id);
		create(dto);
		if(null != generator){
			return getPkValue(dto).toString();
		}else{
			// 如果没有设置IdGenerator, 说明用的是自增长的主键, 或者是sequence
			// 这个方法只有在保证事务隔离的前提下才可以, 否则会因为多线程而取到错误 的结果 
			String tableName = tableObject.getName();;
			String pkColumnName = tableObject.getPkColumn().getName();
			String id = (String)getJdbcTemplate().queryForObject("select max("+pkColumnName+") from " + tableName, String.class);
			setPkValue(dto, id);
			return id;
		}
	}
	
	public void create(Map beanMap) {
		// TODO 优化
		Assert.notNull(beanMap);
		BaseObject bo = toPojo(beanMap);
		create(bo);
	}
	
	public String createAndId(Map beanMap) {
		// TODO 优化
		Assert.notNull(beanMap);
		BaseObject bo = toPojo(beanMap);
		return createAndId(bo);
	}
	
	public void replace(Map beanMap) {
		ColumnBean column = tableObject.getPkColumn();
		String value = (String) beanMap.get(column.getName());// TODO 这里需要做名字的转换？
		if(null != value){
			remove(value.toString());
		}
		create(beanMap);
	}
	
	public void replace(BaseObject bean) {
		ColumnBean column = tableObject.getPkColumn();
		Object value = getPkValue(bean);
		if(null != value){
			remove(value.toString());
		}
		create(bean);
	}
	
	public void remove(String id){
		String sqlTemplate = getSqlTemplate("removeById");
		String sql = initSqlMapByKey(sqlTemplate, mixDbAndPojo(null), null);
		getJdbcTemplate().update(sql,new Object[]{id});
	}
	
	public void remove(Integer id){
		String sqlTemplate = getSqlTemplate("removeById");
		String sql = initSqlMapByKey(sqlTemplate, mixDbAndPojo(null), null);
		getJdbcTemplate().update(sql,new Object[]{id});
	}
	
	public void remove(String[] ids) {
		if(ids == null || ids.length < 1)
			return;
		for(int i = 0; i < ids.length; i++){
			if(StringUtils.isNotBlank(ids[i])){
				remove(ids[i]);
			}
		}
	}
	
	public void remove(Integer[] ids) {
		if(ids == null || ids.length < 1)
			return;
		for(int i = 0; i < ids.length; i++){
			if( null != ids[i] ){
				remove(ids[i]);
			}
		}
	}
	
	public List query(){
		return query(null, "");
	}
	
	public List query(Object pojoOrMap){
		return query(pojoOrMap, "");
	}
	
	public List query(Object pojoOrMap, int begin, int interval, String order) {
		return query(pojoOrMap, null, begin, interval, order);
	}
	
	public List query(Object pojoOrMap, String order) {
		return query(pojoOrMap, null, order);
	}
	
	public List query(Object pojoOrMap, String criteria, String order){
		return query(pojoOrMap, criteria, -1, -1, order);
	}
	
	public List query(Object pojoOrMap, String criteria, int begin, int interval, String order) {
		
		Map paramMap = new HashMap();
		if( begin >= 0 && interval > 0 ){
			paramMap.putAll(
					ConventionUtils.toMap(
							PARAM_BEGIN_NUM, String.valueOf(begin),
							PARAM_END_NUM, String.valueOf(begin + interval + 1),
							PARAM_INTERVAL, interval)
			);
		}
		
		if(StringUtils.isNotBlank(criteria)){
			paramMap.put(PARAM_CRITERIA, AND + criteria);
		}
		if(StringUtils.isNotBlank(order))
			paramMap.put(PARAM_ORDER_BY, ORDER_BY+order);
		if( null == pojoOrMap ){
			return excuteQueryUsingBaseObject("query", null, paramMap);
		}
		if( pojoOrMap instanceof Map ){
			BaseObject dto = getBaseObjectByPojoPath();
			try {
				BeanUtils.copyProperties(dto, pojoOrMap);
			} catch (IllegalAccessException e) {
			} catch (InvocationTargetException e) {
				// donoting
			}
			return excuteQueryUsingBaseObject("query", dto, paramMap);
		}else if( pojoOrMap instanceof BaseObject ){
			return excuteQueryUsingBaseObject("query", (BaseObject) pojoOrMap, paramMap);
		}else{
			throw new IllegalArgumentException("参数应该是BaseObject或者Map");
		}
	}
	
	public List queryByObjectAndMap(BaseObject paramObject, Map paramMap){
		return query(paramObject, paramMap, -1, -1);
	}
	
	public List query(BaseObject dto, Map queryParamMap, int begin, int interval) {
		Map paramMap = initParamMap(queryParamMap, begin, interval);
		
		return excuteQueryUsingBaseObject("query", dto, paramMap);
	}
	
	public List queryByCriteria(String criteria){
		HashMap paramMap = new HashMap();
		if(StringUtils.isNotBlank(criteria))
			paramMap.put(PARAM_CRITERIA, AND + criteria);
		return excuteQueryUsingBaseObject("queryAll", null, paramMap);
	}
	
	public Integer queryCount(Object criteriaOrbeanOrMap) {
		if(criteriaOrbeanOrMap instanceof String){
			return queryCount(null, (String)criteriaOrbeanOrMap);
		}
		return excuteQueryCount("queryCount", criteriaOrbeanOrMap);
	}
	
	public Integer queryCount(BaseObject dto, Map queryParamMap) {
		Map paramMap = new HashMap();
		paramMap.putAll(queryParamMap);
		if(queryParamMap.containsKey(PARAM_CRITERIA)){
			paramMap.put(PARAM_CRITERIA, AND + queryParamMap.get(PARAM_CRITERIA));
		}
		if(queryParamMap.containsKey(PARAM_ORDER_BY)){
			paramMap.put(PARAM_ORDER_BY, ORDER_BY + queryParamMap.get(PARAM_ORDER_BY));
		}
		queryParamMap.putAll( new BeanMap(dto) );
		return excuteQueryCount("queryCount", queryParamMap);
	}
	
	public Integer queryCount(Object object, String criteria){
		Map paramMap = new HashMap();
		if(StringUtils.isNotBlank(criteria)){
			paramMap.put(PARAM_CRITERIA, AND + criteria);
		}
		
		if( null != object ){
			if( object instanceof Map){
				paramMap.putAll( (Map)object );
			}
			else if( object instanceof BaseObject){
				paramMap.putAll( new BeanMap(object) );
			}
		}
		
		return excuteQueryCount("queryCountByCriteria", paramMap);
	}
	
	//根据map的key拷贝map值到BaseObject对应的属性
	private BaseObject toPojo(Map paramMap){
		BaseObject dto = getBaseObjectByPojoPath();
		ConventionUtils.copy(dto, paramMap);
		return dto;
	}
	
	private Object extractOnlyOneResult(List list) {
		if(list.size() == 1){
			return list.get(0);
		}else if(list.size() == 0){
			return null;
		}else{
			throw new BaseRuntimeException("希望得到一个结果, 但查到了"+list.size()+"个结果!");
		}
	}
	
	public BaseObject queryForBaseObject(Object pojoOrMap){
		List resultList = this.query(pojoOrMap, "");
		if(resultList.size() < 1){
			return null;
		}else if(resultList.size() == 1){
			return (BaseObject)resultList.get(0);
		}else{
			throw new IncorrectResultSizeDataAccessException("查询结果返回不只一条，请检查查询条件是否正确：!",1);
		}
	}
	
	public BaseObject queryForBaseObject(BaseObject baseObject, Map paramMap) {
		List list = queryByObjectAndMap(baseObject, paramMap);
		return (BaseObject) extractOnlyOneResult(list);
	}
	
	public BaseObject queryForBaseObjectByCriteria(String criteria){
		List resultList = this.queryByCriteria(criteria);
		if(resultList.size() < 1){
			return null;
		}else if(resultList.size() == 1){
			return (BaseObject)resultList.get(0);
		}else{
			throw new IncorrectResultSizeDataAccessException("查询结果返回不只一条，请检查查询条件是否正确：!",1);
		}
	}

	public int removeByCondition(BaseObject dto) {
		return excuteUpdate(REMOVE, dto, true);
	}

	public int removeByCondition(Map paramMap) {
		return removeByCondition(toPojo(paramMap));
	}
	
	public int removeByCriteria(String criteria) {
		Map paramMap = new HashMap();
		if(StringUtils.isBlank(criteria)){
			//如果criteria为blank则不进行删除
			throw new BaseRuntimeException("criteria 不能为空");
		}
		paramMap.put(PARAM_CRITERIA, AND + criteria);
		String sqlTemplate = getSqlTemplate(REMOVE);
		String sql = initSqlMapByKey(sqlTemplate, mixDbAndPojo(null), paramMap);
		return getJdbcTemplate().update(sql);
	}
	
	public int update(BaseObject dto) {
		return excuteUpdate(UPDATE_KEY, dto, false);
	}
	
	public int update(Map beanMap) {
		Assert.notNull(beanMap);
		return excuteUpdate(UPDATE_KEY, beanMap, false);
	}

	public void updateNotIgnoreNull(BaseObject dto) {
		excuteUpdateNotIgnoreNull("updateNotIgnoreNull", dto);
	}
	
	private void excuteUpdateNotIgnoreNull(String sqlMapKey, BaseObject dto) {
		RuntimeRowObject row = mixDbAndPojo(dto);
		String sql = getSqlTemplate(sqlMapKey);
		String prepareStatSql = initSqlMapByKey(sql, row, null);
		SqlArgTypeSetter argTypeSetter;
		argTypeSetter = ConventionUtils.getPropertyValuesNotIgnoreNull(dto, this.tableObject, jdbcTypeHandlerFactory, this.conventionStrategy);
		if(logger.isDebugEnabled())
			logger.info("args["+StringUtils.join(argTypeSetter.getArgs(),",")+"]");
		Object[] args = argTypeSetter.getArgs();
		int[] argTypes = argTypeSetter.getArgTypes();
		int optNum = 0;
		if(isExistsClobColumn(argTypes)){
			//clob型需要特殊处理
			optNum = getJdbcTemplate().update(prepareStatSql, args, argTypes);
		}else{
			optNum = getJdbcTemplate().update(prepareStatSql, argTypeSetter.getArgs());
		}
		if(logger.isDebugEnabled())
			logger.info("完成对"+optNum+"条记录的操作！");
	}

	public int excuteUpdate(String sqlMapKey, BaseObject data,BaseObject condition){
		RuntimeRowObject rowObject = new RuntimeRowObject(tableObject, data, condition, this.conventionStrategy);
		String sql = getSqlTemplate(sqlMapKey);
		String sqlMap = initSqlMapByKey(sql, rowObject, null);
		if(logger.isDebugEnabled())
			logger.info(sqlMap);
		SqlArgTypeSetter dataArgTypeSetter;;
		//如果sqlmapkey中包含empty，则更新字段中将会忽略Null和空字符串的字段值
		if(sqlMapKey.indexOf(KEY_EMPTY) > 0){
			dataArgTypeSetter = ConventionUtils.getPropertyValuesIgnoreEmpty(data, tableObject, jdbcTypeHandlerFactory, this.conventionStrategy);
		}else{
			dataArgTypeSetter = ConventionUtils.getPropertyValuesIgnoreNull(data, tableObject, jdbcTypeHandlerFactory, this.conventionStrategy);
		}
		if(dataArgTypeSetter.getArgs().length < 1){
			throw new RuntimeException("没有更新字段！");
		}
		SqlArgTypeSetter conditionArgTypeSetter = ConventionUtils.getPropertyValuesIgnoreEmpty(condition, tableObject, jdbcTypeHandlerFactory, this.conventionStrategy);
		Object[] args = ArrayUtils.addAll(dataArgTypeSetter.getArgs(),conditionArgTypeSetter.getArgs());
		if(logger.isDebugEnabled()){
			logger.info("args["+StringUtils.join(args,",")+"]");
		}
			
		if(isExistsClobColumn(dataArgTypeSetter.getArgTypes())){
			//clob型需要特殊处理
			int[] argTypes = ArrayUtils.addAll(dataArgTypeSetter.getArgTypes(),conditionArgTypeSetter.getArgTypes());
			return getJdbcTemplate().update(sqlMap, args, argTypes);
		}else{
			return getJdbcTemplate().update(sqlMap, args);
		}
	}
	public int updateByConditionIgnoreEmpty(BaseObject data,
			BaseObject condition) {
		return excuteUpdate("updateByConditionIgnoreEmpty", data, condition);
	}

	public int updateByCondtion(BaseObject data, BaseObject condition) {
		return excuteUpdate("updateByConditionIgnoreNull", data, condition);
	}
	
	public int updateByCondition(Map update, Map condition) {
		return excuteUpdate("updateByConditionIgnoreNull", toPojo(update), toPojo(condition));
	}

	public int updateIgnoreEmpty(BaseObject dto) {
		return excuteUpdate("updateIgnoreEmpty", dto, true);
	}
	
	private int executeUpdateBySql(final String sql, final Map argsMap){
		String sqlTemplate = ExpressionUtil.parse(sql,argsMap);
		SqlTemplateParseDTO sqlParseDTO = new SqlTemplateParseDTO(sqlTemplate);
		return getJdbcTemplate().update(
				sqlParseDTO.getParseResult(), 
				sqlParseDTO.getTemplateArgs());
	}
	
	public int executeUpdate(final String sqlKey, final Map argsMap){
		return executeUpdateBySql(processSqlmap(sqlKey), argsMap);
	}
	
	private List excuteQuery4Extend(final String sqlKey, final Map paramMap, final Class elementType){
		
		String sqlTemplate = processSqlmap(sqlKey);
		// String sqlTemplate = getSqlExtendByKey(sqlKey);
		
		return excuteQueryByPrepareStatementSqlTemplate(sqlTemplate, paramMap, elementType);
	}
	
	private List excuteQueryByPrepareStatementSqlTemplate(String sqlTemplate, final Map paramMap, final Class elementType){
        
		String sqlBeforeParepareStatement = parse(sqlTemplate ,paramMap);
		
		String tableName = tableObject.getName();
		
		if(!sqlBeforeParepareStatement.toUpperCase().trim().startsWith("SELECT")){
			sqlBeforeParepareStatement = " SELECT * FROM " + tableName + " " + sqlBeforeParepareStatement;
		}
		
		SqlTemplateParseDTO sqlParseDTO = new SqlTemplateParseDTO(sqlBeforeParepareStatement);
		if(elementType == null){
			List list = getJdbcTemplate().query(
					sqlParseDTO.getParseResult(), 
					sqlParseDTO.getTemplateArgs(), 
					getRowMapper()
					);
			
			processFetchProperties(paramMap, sqlParseDTO.getParseResult(), sqlParseDTO.getTemplateArgs(), list);
			return list;
		}
		else if(elementType == Map.class){
			return getJdbcTemplate().queryForList(
					sqlParseDTO.getParseResult(), 
					sqlParseDTO.getTemplateArgs());
		}else{
			return getJdbcTemplate().queryForList(
					sqlParseDTO.getParseResult(), 
					sqlParseDTO.getTemplateArgs(), 
					elementType);
		}
	}

	private void processFetchProperties(final Map paramMap, String prepareStatSql, Object[] args, List resultList) {
		
		if( null == paramMap || StringUtils.isBlank( (String) paramMap.get(IBaseDAO.PARAM_FETCH_PROPERTIES) ) ){
			return;
		}	
		String fetchProp = (String) paramMap.get(IBaseDAO.PARAM_FETCH_PROPERTIES);
		Object obj = getBaseObjectByPojoPath();
		String[] props = StringUtils.split( fetchProp, "|" );
		for (int i = 0; i < props.length; i++) {
			String joinType = JoinTable.INNER_JOIN;
			// 如果没有分隔符那么默认就是属性
			String prop = props[i].trim();
			// eg. inner join|taskItems|orderby_order_dateline_desc 
			String[] values = StringUtils.split( prop.trim(),  ":");
			String orderClause = "";
			String whereClause = "";
			for (int j = 0; j < values.length; j++) {
				String define = values[j];
				// 处理排序
				if( define.startsWith("order by") ){
					/*
					String[] orders = StringUtils.split(define, "_");
					for (int k = 1; k < orders.length; k++) {
						orderClause += orders[k];
						orderClause += " ";
					}
					*/
					orderClause = define;
				} else if( StringUtils.startsWithIgnoreCase(define, "where ")) {
					whereClause = StringUtils.removeStartIgnoreCase(define, "where ");
				} else if(// 处理join 
					define.trim().indexOf("left join") >= 0 ||
					define.trim().indexOf("right join") >= 0 ||
					define.trim().indexOf("join") == 0 ||
					define.trim().indexOf("inner join") >= 0 
					){
					joinType = define.trim();
				}
				// 如果不是特殊的定义 ,那么就是属性了
				else{
					prop = define;
				}
				
			}
			try {
				Method method =  obj.getClass().getMethod(
						"get" + prop.substring(0, 1).toUpperCase() + prop.substring(1), 
						null
						);
				if( method.isAnnotationPresent(Fetch.class) ){
					
					Fetch fetch = method.getAnnotation( Fetch.class );
					
					JoinTable join = null;
					String primaryKeyName = "";
					String pojoName = "";
					String joinColumnName = fetch.column();
					if(fetch.dao().isAnnotationPresent(DAO.class)){
						DAO dao = (DAO) fetch.dao().getAnnotation(DAO.class);
						TableBean tableBean = this.metaSource.getTableMetaData( dao.table() );
						pojoName = dao.pojo().getName();
						// 父表的主键
						primaryKeyName = tableObject.getPkColumn().getName();
						
						if( StringUtils.isBlank( joinColumnName ) ){
							joinColumnName = primaryKeyName;
						}
						join = new JoinTable(dao.table(), joinColumnName, primaryKeyName, joinType);
						join.setOrderBy( orderClause );
						join.setPojoClass( pojoName );
						join.setTableBean( tableBean );
						join.setTargetProperty( prop );
						join.setWhereClause(whereClause);
					}else{
						throw new BaseRuntimeException("not a DAO class");
					}
					// 
					processJoinResult(prepareStatSql, args, resultList, join);
					
				}
				
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
	}
	
	public List queryForListByBaseObject(final String sqlKey, final BaseObject query){
		Map paramMap = new BeanMap(query);
		return excuteQuery4Extend(sqlKey, paramMap, null);
	}
	
	public List queryForListByBaseObject(final String sqlKey, final BaseObject query, final Class elementType){
		Map paramMap = new BeanMap(query);
		return excuteQuery4Extend(sqlKey, paramMap, elementType);
	}
	
	public List queryForListByMap(final String sqlKey, final Map queryMap){
		return excuteQuery4Extend(sqlKey, queryMap, null);
	}
	
	public List queryForListByMap(final String sqlKey, final Map queryMap, final Class elementType){
		return excuteQuery4Extend(sqlKey, queryMap, elementType);
	}
	
	public BaseObject queryForBaseObject(final String sqlKey, final BaseObject query){
		List resultList = queryForListByBaseObject(sqlKey, query);
		if(resultList.size() < 1)
			return null;
		else if(resultList.size() == 1){
			return (BaseObject)resultList.get(0);
		}else{
			throw new IncorrectResultSizeDataAccessException("查询结果返回不只一条，请检查查询条件是否正确：!",1);
		}
	}
	
	public BaseObject queryForBaseObject(final String sqlKey, final Map queryMap){
		List resultList = queryForListByMap(sqlKey, queryMap);
		if(resultList.size() < 1)
			return null;
		else if(resultList.size() == 1){
			return (BaseObject)resultList.get(0);
		}else{
			throw new IncorrectResultSizeDataAccessException("查询结果返回不只一条，请检查查询条件是否正确：!",1);
		}
	}
	
	public Object queryForObject(final String sqlKey, final BaseObject query, Class requiredType){
		List resultList = queryForListByBaseObject(sqlKey, query,requiredType);
		if(resultList.size() < 1){
			return null;
		}else if(resultList.size() == 1){
			try{
				return resultList.get(0);
			}catch(ClassCastException ex){
				throw new ClassCastException("返回值不是"+requiredType.getName()+"类型，请检查!");
			}
		}else{
			throw new IncorrectResultSizeDataAccessException("查询结果返回不只一条，请检查查询条件是否正确：!",1);
		}
	}
	
	public Object queryForObject(final String sqlKey, final Map queryMap, Class requiredType){
		List resultList = queryForListByMap(sqlKey, queryMap, requiredType);
		if(resultList.size() < 1){
			return null;
		}else if(resultList.size() == 1){
			try{
				return resultList.get(0);
			}catch(ClassCastException ex){
				throw new ClassCastException("返回值不是"+requiredType.getName()+"类型，请检查!");
			}
		}else{
			throw new IncorrectResultSizeDataAccessException("查询结果返回不只一条，请检查查询条件是否正确：!",1);
		}
	}
	
	private String parse(String templateStr, Object vObject) {

		templateStr = processSqlmap(templateStr);
		
		Template t;
		StringWriter stringWriter = new StringWriter();
		Configuration cfg = new Configuration();
		cfg.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
		cfg.setEncoding(Locale.getDefault(), "GBK");
		
		try {
			//freemarker在解析数据格式的时候，自动默认将数字按3为分割（1，000），需要禁用掉
			String disableNumberParserStr = "<#setting number_format=\"#\">";
			t = new Template("dao template parser",
					new StringReader(disableNumberParserStr + templateStr), cfg);

			t.process(vObject, stringWriter);
			stringWriter.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw new BaseRuntimeException("获取表达式内容出现异常", e);
		}
		return stringWriter.toString();
	}

	private String processSqlmap(String templateStr) {
		// 从sqlmap文件中取得sql
        if( getUserSqlMap().containsKey( templateStr ) ) {
        	String key = templateStr;
        	templateStr = getUserSqlMap().get( templateStr );
            logger.debug("从SQLMAP中获取sql模板:");
            logger.debug("key:" + key );
            logger.debug("sql:" + templateStr );
        }
		return templateStr;
	}
	
	public List queryByTpl(String sqlTemplate){
		return queryByTpl(sqlTemplate, null,  -1, -1);
	}
	
	public List queryByTpl(String sqlTemplate, int begin, int interval) {
		return queryByTpl(sqlTemplate, null, begin, interval);
	}

	public List queryByTpl(String sqlTemplate, Object paramObject) {
		return queryByTpl(sqlTemplate, paramObject, new HashMap(), -1, -1);
	}

	public List queryByTpl(String sqlTemplate, Object paramObject, int begin, int end) {
		return queryByTpl(sqlTemplate, paramObject, new HashMap(), begin, end);
	}

	public List queryByTpl(String sqlTemplate, Object paramObject, Map paramMap) {
		return queryByTpl( sqlTemplate, paramObject, paramMap, null, -1, -1 );
	}

	public List queryByTpl(String sqlTemplate, Object paramObject, Map paramMap, int begin, int end) {
		return queryByTpl(sqlTemplate, paramObject, paramMap, null, begin, end);
	}
	
	public List queryByTpl(String sqlTemplate, DTOCallbackHandler callbackHandler){
		return queryByTpl(sqlTemplate, null, callbackHandler, -1, -1);
	}
	
	public List queryByTpl(String sqlTemplate, DTOCallbackHandler callbackHandler, int begin, int end) {
		return queryByTpl(sqlTemplate, null, callbackHandler, begin, end);
	}

	public List queryByTpl(String sqlTemplate, Object paramObject, DTOCallbackHandler callbackHandler) {
		return queryByTpl(sqlTemplate, paramObject, new HashMap(), callbackHandler, -1, -1);
	}
	
	public List queryByTpl(String sqlTemplate, Object paramObject, DTOCallbackHandler callbackHandler, int begin, int interval) {
		return queryByTpl(sqlTemplate, paramObject, new HashMap(), callbackHandler, begin, interval);
	}

	public List queryByTpl(String sqlTemplate, Object paramObject, Map paramMap, DTOCallbackHandler callbackHandler, int begin, int interval) {
		sqlTemplate = processSqlmap(sqlTemplate);
		if(null == paramMap){ 
			paramMap = new HashMap(); 
		}
		paramMap.putAll( initParamMap(paramObject, begin, interval) );
		
		if(begin > 0 || interval > 0){
			sqlTemplate = SqlStrategyFactory.getBean(getDataSource()).paginate(sqlTemplate);
		}
		List results = excuteQueryByPrepareStatementSqlTemplate(sqlTemplate, paramMap, null);
		if(null != callbackHandler){
			for (Iterator iterator = results.iterator(); iterator.hasNext();) {
				Object object = (Object) iterator.next();
				callbackHandler.processDTO(object);
			}
		}
		
		return results;
	}

    private Map<String, String> getUserSqlMap() {
    	
    	InputStream input = null;
    	if( null != this.userSqlMap ){
	    	// 如果在调式模式下，那么判断是否文件进行过修改，如果修改过那么把sqlmap清空，重新加载
	    	if( "true".equals(GlobalConfig.get("debug")) ){
	    		
	    		
	    		
	    		URL url = getClass().getResource( getClass().getSimpleName() + ".sqlmap" );
	    		// 有并且不在jar包中
	    		if( null != url && url.getFile().indexOf(".jar") < 0 ){
	    			Long currentLastmodify = new File( url.getPath() ).lastModified();
	    			if( null != lastModifyTime ){
	    				if( lastModifyTime.longValue() != currentLastmodify.longValue() ){
	    					userSqlMap = null;
	    				}
	    			}else{
	    			}
	    			lastModifyTime = currentLastmodify;
	    		}
	    	}
    	}
    	// 第一次访问的时候进行初始化
    	if( null == this.userSqlMap ){
    		userSqlMap = new HashMap();
    		try {
    			if(StringUtils.isBlank( this.mappingFilePath )){
    				URL url = getClass().getResource( getClass().getSimpleName() + ".sqlmap" );
    				lastModifyTime = new File( url.getPath() ).lastModified();
    				
    				// input = getClass().getResourceAsStream( getClass().getSimpleName() + ".sqlmap" );
    				input = new FileInputStream( url.getPath() );
    				// input = getClass().getResourceAsStream( getClass().getSimpleName() + ".sqlmap" );
    			}else{
    				input = Thread.currentThread().getContextClassLoader().getResourceAsStream( this.mappingFilePath );
    			}
    		} catch (Exception e) {
    			logger.debug(" no sqlmapping file find! ");
    		}
    		if( null == input ){
        		logger.debug(" no sqlmapping file find! ");
            }else{
            	try {
					char[] chars = IOUtils.toCharArray(input, "UTF-8");
					StringBuffer s = new StringBuffer();
					
					char status = '=';
					String key = "";
					
					boolean commetStatus = false;
					for (int i = 0; i < chars.length; i++) {
						
						
						if( !commetStatus && '/' == chars[i] && '*' == chars[i+1] ){
							
							commetStatus = true;
							
						}else if( commetStatus && '/' == chars[i] && '*' == chars[i-1] ){
							
							commetStatus = false;
							
						}else if( !commetStatus ){
							if( '=' == status && chars[i] == status ){
								status = ';';
								key = s.toString();
								s = new StringBuffer();
							}else if( ';' == status && chars[i] == status ){
								status = '=';
								userSqlMap.put( StringUtils.trim( key ), StringUtils.trim( s.toString() ) );
								s = new StringBuffer();
							}else{
								s.append(chars[i]);
							}
						}
					}
					input.close();
				} catch (IOException e) {
					logger.debug(" load sqlmapping file error! ");
				}
				
            }
    	}
    	
    	return userSqlMap;
    }

    public Object queryForBaseObjectByTpl(String sqlTemplate) {
		return queryForBaseObjectByTpl(sqlTemplate, null);
	}
	
	public Object queryForBaseObjectByTpl(String sqlTemplate, Object paramObject) {
		List list = queryByTpl(sqlTemplate, paramObject);
		if(list.size() == 0){
			return null;
		}
		return list.get(0);
	}
	
	private Map initParamMap(Object paramObject, int begin, int interval) {
		Map paramMap = new HashMap();
		if(null == paramObject){
			// do nothing
		}else if(paramObject instanceof Map){
			// do nothing
			paramMap.putAll((Map) paramObject);
		}else{
			paramMap.putAll( new BeanMap(paramObject) );
		}
		
		if (begin > 0) {
			paramMap.put(PARAM_BEGIN_NUM, begin);
		}
		if (interval > 0) {
			paramMap.put(PARAM_INTERVAL, interval);
		}
		if (begin >= 0 && interval > 0) {
			paramMap.put(PARAM_END_NUM, String.valueOf(begin + interval + 1));
		}
		if(paramMap.containsKey(PARAM_CRITERIA)){
			paramMap.put(PARAM_CRITERIA, AND + paramMap.get(PARAM_CRITERIA));
		}
		if(paramMap.containsKey(PARAM_ORDER_BY)){
			paramMap.put(PARAM_ORDER_BY, ORDER_BY + paramMap.get(PARAM_ORDER_BY));
		}
		return paramMap;
	}

	public Object queryForObjectByTpl(String sqlTemplate, Object paramObject, Class requireType) {
		sqlTemplate = processSqlmap(sqlTemplate);
		Map paramMap = initParamMap(paramObject, -1, -1);
		
		List results = excuteQueryByPrepareStatementSqlTemplate(sqlTemplate, paramMap, requireType);
		return extractOnlyOneResult(results);
	}
	
	public List queryForListByTpl(String sqlTemplate, Object paramObject, Class requireType) {
		sqlTemplate = processSqlmap(sqlTemplate);
		Map paramMap = initParamMap(paramObject, -1, -1);
		
		List results = excuteQueryByPrepareStatementSqlTemplate(sqlTemplate, paramMap, requireType);
		return results;
	}
	
	public List queryForListByTpl(String sqlTemplate, Object paramObject, Class requireType, int begin, int interval) {
		sqlTemplate = processSqlmap(sqlTemplate);
		Map paramMap = initParamMap(paramObject, begin, interval);
		if(begin > 0 || interval > 0){
			sqlTemplate = SqlStrategyFactory.getBean(getDataSource()).paginate(sqlTemplate);
		}
		List results = excuteQueryByPrepareStatementSqlTemplate(sqlTemplate, paramMap, requireType);
		return results;
	}
	
	public Integer queryCountByTpl(String sqlTemplate, Object paramObject) {
		sqlTemplate = processSqlmap(sqlTemplate);
		if(!sqlTemplate.toUpperCase().trim().startsWith("SELECT")){
			String tableName = tableObject.getName();
			sqlTemplate = " SELECT count(*) FROM " + tableName + " " + sqlTemplate;
		}
		return (Integer) queryForObjectByTpl(sqlTemplate, paramObject, Integer.class);
	}
	
	public Integer queryCountByTpl(String sqlTemplate, Object... paramObject) {
		
		return queryCountByTpl(sqlTemplate, ConventionUtils.toMap(paramObject));
	}

	public Integer queryCountByTpl(String sqlTemplate) {
		sqlTemplate = processSqlmap(sqlTemplate);
		if(!sqlTemplate.toUpperCase().trim().startsWith("SELECT")){
			String tableName = tableObject.getName();
			sqlTemplate = " SELECT count(*) FROM " + tableName + " " + sqlTemplate;
		}
		return (Integer) queryForObjectByTpl(sqlTemplate, null, Integer.class);
	}

	public int updateByTpl(String sqlTemplate, Object paramObject){
		String sqlBeforeParepareStatement = parse(sqlTemplate ,paramObject);
		SqlTemplateParseDTO sqlParseDTO = new SqlTemplateParseDTO(sqlBeforeParepareStatement);
		return getJdbcTemplate().update(sqlParseDTO.getParseResult(), sqlParseDTO.getTemplateArgs());
	}
	
	public void updateByTpl(String sqlTemplate, Object... paramObjects){
		
		updateByTpl(sqlTemplate, null == paramObjects? null : ConventionUtils.toMap(paramObjects));
	}

}
