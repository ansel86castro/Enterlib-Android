package com.enterlib.fields;

import android.view.View;

public class GenericField extends Field {

	public GenericField() {
		super();
	}

	public GenericField(View view, boolean required) {
		super(view, required);

	}

	public GenericField(View view, String display, boolean required) {
		super(view, display, required);

	}

	public GenericField(View view, String valueBinding, String display,
			boolean required) {
		super(view, valueBinding, display, required);

	}

	public GenericField(View view, String valueBinding) {
		super(view, valueBinding);

	}

	public GenericField(View view) {
		super(view);

	}

	@Override
	protected void onSetErrorMessage(String errorMessage) {
	}

	@Override
	protected Object getViewValue() {
		return null;
	}

	@Override
	protected void setViewValue(Object value) {
	}

}
