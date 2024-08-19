package com.enterlib.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.enterlib.fields.Field;
import com.enterlib.validations.IValueValidator;

/**
 * Defines the Class of an {@link IValueValidator} that will be added to the
 * {@link Field} corresponding to a class member marked with {@link FormField}
 * 
 * @author Ansel
 *
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValueValidatorClass {

	/**
	 * @return The validator class that implements {@link IValueValidator}
	 */
	public Class<?> value();
}
