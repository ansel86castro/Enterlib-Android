package com.enterlib.mvvm;

import com.enterlib.fields.Form;

public interface IFormView extends IView {

	Form getForm();

	boolean validate();

}