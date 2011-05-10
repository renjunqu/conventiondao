/* 
 * Copyright (c) 2006 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 */
package com.rework.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rework.joss.persistence.IBaseDAO;

/**
 * 
 * @author zhangsh
 * @since 2006-6-7
 */
public abstract class UtilMisc {
	
	
	public static Map fetch(String condition){
		return UtilMisc.toMap( IBaseDAO.PARAM_FETCH_PROPERTIES, condition );
	}
	
	public static Map toMap(Object... objs) {
		Map map = new HashMap();
		Object key = null;
		for (int i = 0; i < objs.length; i++) {
			if(i%2 == 0){
				key = objs[i];
			}else{
				map.put(key, objs[i]);
			}
		}
		return map;
	}
	
	public static Map m(Object... objs) {
		return toMap(objs);
	}
	
	public static Map map(Object... objs) {
		return toMap(objs);
	}
	
	public static List toList(Object... objs) {
		List result = new ArrayList();
		for (int i = 0; i < objs.length; i++) {
			result.add(objs[i]);
		}
		return result;
	}

	public static List list(Object... objs) {
		return toList(objs);
	}
}
