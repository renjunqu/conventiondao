package com.rework.joss.persistence.convention.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;



@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface DBColumn {

	String comment() default "";

	String columnName() default "";

	int columnType();

	int length();

	int precision()  default 0;

	int scale()  default 0;

	boolean primaryKey() default false;

	boolean nullable() default false;

	boolean autoincrement() default false;

	boolean unicode() default true;

	String defaultValue() default "";

}
