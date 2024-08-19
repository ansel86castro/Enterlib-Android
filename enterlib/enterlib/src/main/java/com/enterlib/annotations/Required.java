package com.enterlib.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.enterlib.validations.validators.RequiredValidator;

/**
 * Mark a field as required. this annotation must be used in with the
 * {@link FormField} annotation
 * 
 * @author Ansel
 *
 */
@ValueValidatorClass(RequiredValidator.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Required {
	
}
