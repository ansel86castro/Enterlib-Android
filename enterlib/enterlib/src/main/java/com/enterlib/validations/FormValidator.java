package com.enterlib.validations;

import com.enterlib.fields.Field;
import com.enterlib.fields.Form;

public abstract class FormValidator extends Validator {
	Form form;

	
	
	public FormValidator() {
		// TODO Auto-generated constructor stub
	}

	public FormValidator(String errorMessage) {
		super(errorMessage);
		// TODO Auto-generated constructor stub
	}

	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	public Field getField(int id) {
		return form.getFieldById(id);
	}

	@Override
	public abstract boolean validate();

}
