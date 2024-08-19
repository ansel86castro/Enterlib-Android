package com.enterlib.validations.validators;

import com.enterlib.StringUtils;

public class RequiredValidator extends ValueValidator {

	public RequiredValidator() {
		super("Requerido");
	}

	public RequiredValidator(String errorMessage) {
		super(errorMessage);
		// TODO Auto-generated constructor stub
	}

	// public boolean validateValue(String value) {
	// return !StringUtils.isNullOrWhitespace(value);
	// }

	@Override
	public boolean validateValue(Object value) {
		if (value instanceof String) {
			return !StringUtils.isNullOrWhitespace((String) value);
		} else if (value != null) {
			return true;
		}
		return false;
	}

}
