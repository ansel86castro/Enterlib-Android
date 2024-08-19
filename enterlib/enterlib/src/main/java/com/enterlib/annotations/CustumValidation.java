package com.enterlib.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface CustumValidation {

	public Class<?> validator();

	public String method() default "validate";
}
