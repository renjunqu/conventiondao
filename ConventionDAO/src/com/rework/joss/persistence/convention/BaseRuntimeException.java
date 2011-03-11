package com.rework.joss.persistence.convention;

import org.springframework.core.NestedRuntimeException;

public class BaseRuntimeException extends NestedRuntimeException {

	public BaseRuntimeException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public BaseRuntimeException(String msg) {
		super(msg);
	}

}
