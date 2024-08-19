package com.enterlib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnMap {
	
	public String column() default "";
	
	public boolean writable() default true;	
	
	public boolean key() default false;
	
	public int order() default 0;

	public boolean nonMapped() default false;
}
