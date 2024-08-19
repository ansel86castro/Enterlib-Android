package com.enterlib.validations.validators;

import com.enterlib.validations.IValueValidator;

public abstract class ValueValidator implements IValueValidator {

	private String errorMessage;

	public ValueValidator() {
		errorMessage = "Invalid Format";
	}

	public ValueValidator(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public abstract boolean validateValue(Object value);

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	protected void setErrorMessage(String value) {
		errorMessage = value;
	}

}
