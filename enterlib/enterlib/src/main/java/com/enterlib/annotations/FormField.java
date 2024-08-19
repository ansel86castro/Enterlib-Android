package com.enterlib.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.enterlib.fields.Field;

/**
 * This is used in instance fields of a Model class for automatically
 * {@link Field} generation. This functionality is not implemented yet.
 * 
 * @author Ansel
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface FormField {

	public String name();

	public String display();

	public boolean required() default false;

	public String description() default "";
}
