package com.enterlib.filtering;

import java.util.Date;

import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

import com.enterlib.converters.Converters;
import com.enterlib.converters.DateConverter;
import com.enterlib.converters.IValueConverter;
import com.enterlib.fields.DatePickerButtonField;
import com.enterlib.widgets.DatePickerButton;

public class DateFilterCondition<T> extends FilterCondition {

	private DateConverter filterDateConverter = new DateConverter("yyyy-MM-dd HH:mm:ss");

	public DateFilterCondition(String queryName, String queryHint,
			DatePickerButtonField field) {
		super(queryName, queryHint, field);
	}

	public DateFilterCondition(Context context, String queryName,
			String queryHint, IValueConverter dateConverter) {
		super(queryName, queryHint, null);

		DatePickerButton datePicker = new DatePickerButton(context);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_VERTICAL|Gravity.FILL_HORIZONTAL;
		datePicker.setLayoutParams(params);

		setField(new DatePickerButtonField(datePicker,
				"DateFilterCondition" + queryHint));

		if (dateConverter == null) {
			dateConverter = Converters.StringToDateConverter;
		}
		this.converter = dateConverter;
	}

	public DateFilterCondition(Context context, String queryHint,
			IValueConverter dateConverter) {
		this(context, null, queryHint, dateConverter);
	}

	public DateFilterCondition(Context context, String queryHint) {
		this(context, null, queryHint, Converters.StringToDateConverter);
	}
	
	public DateFilterCondition(Context context, String queryName, String queryHint, int operation) {
		this(context, queryName, queryHint, null);
		this.filterOp = operation;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean eval(Object item) {
		Date date = (Date) queryValue;
		if (date == null) {
			return true;
		}
		return eval(date, (T) item);
	}

	protected boolean eval(Date date, T item){return false;}

	@Override
	public String getFilterExpression() {
		if(queryValue == null)
			return null;
		return String.format("%s %s '%s'", queryName,
				getOpString(),
				filterDateConverter.getString((Date)queryValue));
	}
}
