package com.enterlib.validations;

import com.enterlib.fields.Field;

/**
 * @author ansel Represent a {@link Field} value validator
 * */
public interface IValueValidator {

	/** returns true if the value is valid */
	boolean validateValue(Object value);

	/**
	 * return the error message to display in the View's UI if
	 * {@code validateValue} returns false
	 * */
	String getErrorMessage();

}
