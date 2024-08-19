package com.enterlib.exceptions;

import com.enterlib.fields.Field;
import com.enterlib.fields.Form;
import com.enterlib.validations.ErrorInfo;

/**
 * {@link Exception} that is thrown when business validations fails. The
 * validations errors are contained in an {@link ErrorInfo} object that you can
 * access calling the {@code getError()} method
 * */
public class ValidationException extends InvalidOperationException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private ErrorInfo errorInfo;

	public ValidationException(String field, String errorMessage) {
		super(errorMessage);

		errorInfo = new ErrorInfo();
		errorInfo.add(field, errorMessage);
	}

	public ValidationException(ErrorInfo errorInfo) {
		super(errorInfo.getAllErrors());

		this.errorInfo = errorInfo;
	}
	
	public ValidationException() {
		errorInfo = new ErrorInfo();
	}

	/**
	 * Returns the validations errors. You can automaticaly bind this errors to
	 * Forms {@link Field} calling the {@link Form}.{@code setFieldErrors method}
	 */
	public ErrorInfo getError() {
		return errorInfo;
	}

	public ValidationException putError(String property, String errorMessage){
		errorInfo.add(property, errorMessage);
		return this;
	}
}
