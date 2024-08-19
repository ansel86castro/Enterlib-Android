package com.enterlib.fields;

import java.util.Date;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.enterlib.exceptions.ConversionFailException;
import com.enterlib.widgets.DatePickerButton;
import com.enterlib.widgets.DateTimePickerButton;
import com.enterlib.widgets.TimePickerButton;

/**
 * This is a {@link Form} field that displays a {@link DateTimePickerButton}
 * like a {@link DatePickerButton} or {@link TimePickerButton} in the UI
 */
public class DatePickerButtonField extends TextViewField {

	public DatePickerButtonField() {
		super();
	}

	public DatePickerButtonField(DateTimePickerButton view, boolean inRequired) {
		super(view, inRequired);
	}

	/**
	 * @param view
	 * @param valueBinding
	 * @param display
	 * @param required
	 */
	public DatePickerButtonField(DateTimePickerButton view, String valueBinding, String display, boolean required) {
		super(view, valueBinding, display, required);
	}

	public DatePickerButtonField(DateTimePickerButton view, String display, boolean required) {
		super(view, display, required);
	}

	public DatePickerButtonField(DateTimePickerButton view, String valueBinding) {
		super(view, valueBinding);
	}

	public DatePickerButtonField(DateTimePickerButton view) {
		super(view);
	}

	public DateTimePickerButton getDatePicker() {
		return (DateTimePickerButton) getView();
	}

	@Override
	protected Object getViewValue() {
		try {
			return getDatePicker().getDate();
		} catch (ConversionFailException e) {
			Log.d("DatePickerButton", e.getMessage());
			return null;
		}
	}

	@Override
	protected void setViewValue(Object value) {
		if (value  == null){
			getDatePicker().setDate(null);
		}
		else if (value instanceof Date) {
			getDatePicker().setDate((Date) value);
		} else if (value instanceof String) {
			getDatePicker().setDateString((String)value);
		}
	}

	public Date getDate() {
		try {
			return getDatePicker().getDate();
		} catch (ConversionFailException e) {
			Log.d("DatePickerButton", e.getMessage());
			return null;
		}
	}

}
