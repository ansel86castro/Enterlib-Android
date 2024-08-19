package com.enterlib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeingKey {

	public String field() default "";
	
	public Class<?> model();
	
	public String update() default "NO ACTION";
	
	public String delete() default "NO ACTION";
}
