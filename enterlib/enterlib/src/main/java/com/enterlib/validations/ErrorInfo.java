package com.enterlib.validations;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.enterlib.app.UIUtils;
import com.enterlib.exceptions.ValidationException;
import com.enterlib.widgets.ErrorPopup;

import android.content.Context;

public class ErrorInfo implements Serializable {

	
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private HashMap<String, ValidationResult> validationMessages;	
	private ArrayList<String> errorMessages;

	public ErrorInfo() {
		validationMessages = new HashMap<String, ValidationResult>();		
	}

	public void add(String field, String errorMessage) {
		if (!validationMessages.containsKey(field)) {
			validationMessages.put(field, new ValidationResult(field, errorMessage));
		} else {
			ValidationResult messages = validationMessages.get(field);
			messages.add(errorMessage);
		}
	}

	public ErrorInfo addError(String field, String errorMessage) {
		add(field, errorMessage);
		return this;
	}

	public ErrorInfo addError(String errorMessage) {
		add(errorMessage);
		return this;
	}
	

	public void add(String error) {
		if(errorMessages == null)
			errorMessages = new ArrayList<String>();
		errorMessages.add(error);		
	}

	public boolean containsError(String field) {
		return validationMessages.containsKey(field);
	}

	public boolean containsErrors() {
		return validationMessages.size() > 0 || (errorMessages !=null && errorMessages.size() > 0);
	}

	public ValidationResult getValidationResult(String field) {
		if (!validationMessages.containsKey(field)) {
			return null;
		}
		return validationMessages.get(field);
	}

	public Collection<ValidationResult> getValidationResults() {
		return validationMessages.values();
	}

	public String getErrorMessage(String field) {
		ValidationResult result = getValidationResult(field);
		if (result == null) {
			return "";
		}
		return result.getError();
	}
	
	public String getErrorMessage(Context context, String field) {
		ValidationResult result = getValidationResult(field);
		if (result == null) {
			return "";
		}
		return result.getError(context);
	}

	public String getAllErrors() {
		StringBuilder sb = new StringBuilder();
		if(errorMessages!=null){
			for(String err : errorMessages){
				sb.append(err);
				sb.append("\n");
			}
		}
		
		for (ValidationResult field : getValidationResults()) {
			sb.append(field.getError());
			sb.append("\n\n");
		}
		return sb.toString();
	}
	
	public String getAllErrors(Context context) {
		StringBuilder sb = new StringBuilder();
		if(errorMessages!=null){
			sb.append(getGeneralError(context));
		}
		
		for (ValidationResult field : getValidationResults()) {
			sb.append(field.getError(context));
			sb.append("\n\n");
		}
		return sb.toString();
	}

	public void clearErrors() {					
		validationMessages.clear();
		
		if(errorMessages!=null){
			errorMessages.clear();
		}
	}

	public void assertError() throws ValidationException {
		if (containsErrors()) {
			throw new ValidationException(this);
		}
	}

	public String getGeneralError(Context context) {
		if(errorMessages == null)
			return "";
		
		StringBuilder sb = new StringBuilder();
		for(String err : errorMessages){
			int resId = UIUtils.findResourceId(context, err, "string");
			if(resId == 0)
				sb.append(err);
			else
				sb.append(context.getString(resId));
			
			sb.append("\n");
		}
		return errorMessages.toString();
	}
	
	public String getGeneralError() {
		if(errorMessages == null)
			return "";
		
		StringBuilder sb = new StringBuilder();
		for(String err : errorMessages){			
			sb.append(err);						
			sb.append("\n");
		}
		return errorMessages.toString();
	}

}
