package com.enterlib.validations.validators;

public abstract class StringValidator extends ValueValidator {

	public StringValidator() {

	}

	public StringValidator(String errorMessage) {
		super(errorMessage);
	}

	@Override
	public final boolean validateValue(Object value) {
		try {
			return validateValue((String) value);
		} catch (ClassCastException e) {
			return false;
		}
	}

	public abstract boolean validateValue(String value);

}
