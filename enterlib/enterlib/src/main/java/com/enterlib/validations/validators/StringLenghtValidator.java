package com.enterlib.validations.validators;

import com.enterlib.StringUtils;

public class StringLenghtValidator extends StringValidator {
	int maxValue;
	int minValue;

	public StringLenghtValidator() {
		super("valor fuera de rango");
	}

	public StringLenghtValidator(int maxValue, int minValue) {
		super("valor fuera de rango");
		this.maxValue = maxValue;
		this.minValue = minValue;
	}

	public StringLenghtValidator(int maxValue, int minValue, String errorMessage) {
		super(errorMessage);

		this.maxValue = maxValue;
		this.minValue = minValue;
	}

	public StringLenghtValidator(String errorMessage) {
		super(errorMessage);
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public int getMinValue() {
		return minValue;
	}

	public void setMinValue(int minValue) {
		this.minValue = minValue;
	}

	@Override
	public boolean validateValue(String value) {
		if (StringUtils.isNullOrWhitespace(value)) {
			return false;
		}

		int length = value.length();
		if (length < minValue || length > maxValue) {
			return false;
		}

		return true;
	}

}
