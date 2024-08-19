package com.enterlib.validations.validators;

import com.enterlib.fields.Field;
import com.enterlib.validations.FormValidator;

public class EqualStringValidator extends FormValidator {

	
	private String propertyA;
	private String propertyB;
	private String errorMessage;
	
	public EqualStringValidator(String propertyA, String propertyB, String errorMessage) {
		super(errorMessage);
		
		this.propertyA = propertyA;
		this.propertyB = propertyB;
		this.errorMessage = errorMessage;
	}

	@Override
	public boolean validate() {
		Field a = getForm().getFieldByBinding(propertyA);
		Field b = getForm().getFieldByBinding(propertyB);
		
		Object valueA = a.getValue();
		Object valueB = b.getValue();
		
		if(valueA!=null && !valueA.equals(valueB)){
			b.setErrorMessage(getErrorMessage());
			setErrorMessage(errorMessage);
			return false;
		}
		b.setErrorMessage(null);
		setErrorMessage(null);
		return true;
				
	}

}
