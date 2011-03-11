/* 
 * Copyright (c) 2004-2007 HEER Software, Inc. All rights reserved.
 *
 * This software consists of contributions made by many individuals
 * on behalf of Heer R&D.  For more information,
 * please see <http://www.heerit.com/>.
 *
 */
package com.rework.joss.persistence.convention.type;

import com.rework.joss.persistence.convention.BaseRuntimeException;

public class SqlMapException extends BaseRuntimeException {

	public SqlMapException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public SqlMapException(String msg) {
		super(msg);
	}

}
