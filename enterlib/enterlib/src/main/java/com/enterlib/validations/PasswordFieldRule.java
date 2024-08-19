package com.enterlib.validations;

import android.os.Handler;
import android.widget.TextView;

/*
 * A class simulating multi-field validation
 */
public class PasswordFieldRule implements IValidator {
	private TextView password1;
	private TextView password2;
	private String errorMessage;

	public PasswordFieldRule(TextView p1, TextView p2) {
		password1 = p1;
		password2 = p2;
	}

	@Override
	public boolean validate() {
		String p1 = password1.getText().toString();
		String p2 = password2.getText().toString();

		if (p1.equals(p2)) {
			return true;
		}

		// They are not the same
		setErrorMessage("Sorry, password values don't match!");
		return false;
	}

	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String value) {
		this.errorMessage = value;
		password2.setError(value);
	}

	@Override
	public boolean validateAsync(Handler handler) {
		String p1 = password1.getText().toString();
		String p2 = password2.getText().toString();

		if (p1.equals(p2)) {
			return true;
		}

		// They are not the same
		errorMessage = "Sorry, password values don't match!";
		handler.post(new Runnable() {
			@Override
			public void run() {
				password2.setError(errorMessage);
			}
		});
		return false;
	}
}