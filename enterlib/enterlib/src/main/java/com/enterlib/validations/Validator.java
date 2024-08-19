package com.enterlib.validations;

import android.os.Handler;

public abstract class Validator implements IValidator {

	private String errorMessage;

	public Validator() {
		super();
	}

	public Validator(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public boolean validateAsync(Handler handler) {
		return validate();
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	protected void setErrorMessage(String value) {
		errorMessage = value;
	}

}
