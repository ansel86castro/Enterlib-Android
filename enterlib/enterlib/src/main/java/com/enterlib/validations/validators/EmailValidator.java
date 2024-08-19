package com.enterlib.validations.validators;

import com.enterlib.R;

import android.content.Context;


public class EmailValidator extends RegExValueValidator {

	public EmailValidator(String errorMessage, boolean allowSeveralAddress ,String addressSeparator) {
		super("\\s*((\\w|-)+)(\\.((\\w|-)+))*@((\\w|-)+)(\\.((\\w|-)+))*\\s*" + (allowSeveralAddress?
			 "("+addressSeparator+"\\s*((\\w|-)+)(\\.((\\w|-)+))*@((\\w|-)+)(\\.((\\w|-)+))*\\s*"+")*":""), errorMessage);
				
	}
	
	public EmailValidator(String errorMessage) {
		this(errorMessage, false, null);
	}
	
	public EmailValidator(Context context){
		this(context.getString(R.string.err_invalid_email));
	}
	
}
