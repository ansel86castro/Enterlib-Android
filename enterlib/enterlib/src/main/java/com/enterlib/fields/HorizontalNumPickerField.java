package com.enterlib.fields;

import com.enterlib.databinding.BindingProperty;
import com.enterlib.widgets.HorizontalNumPicker;

public class HorizontalNumPickerField extends Field {

	private HorizontalNumPicker picker;

	public HorizontalNumPickerField(HorizontalNumPicker view) {
		super(view);
		
		this.picker = view;
	}

	@Override
	protected void onViewChanged() {	
		super.onViewChanged();
		this.picker = (HorizontalNumPicker) getView();
	}
	
	@Override
	protected void onSetErrorMessage(String errorMessage) {
		picker.setError(errorMessage);
	}

	@Override
	protected Object getViewValue() {
		return picker.getValue();
	}

	@Override
	protected void setViewValue(Object value) {
		picker.setValue((Integer) value);

	}

}
