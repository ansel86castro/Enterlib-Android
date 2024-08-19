package com.enterlib.filtering;

import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;

import com.enterlib.converters.IValueConverter;
import com.enterlib.fields.CompoundButtonField;

public class CheckBoxFilterCondition<T> extends FilterCondition {

	public CheckBoxFilterCondition(Context context, String queryName,
			String queryHint, IValueConverter converter) {
		super(queryName, queryHint, null);

		CheckBox view = new CheckBox(context);
		view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));

		setField(new CompoundButtonField(view, "FilterCondition" + queryHint));
		this.converter = converter;

	}

	public CheckBoxFilterCondition(Context context, String queryHint) {
		this(context, null, queryHint, null);
	}

	public CheckBoxFilterCondition(Context context, String queryName,
			String queryHint) {
		this(context, queryName, queryHint, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean eval(Object item) {
		Boolean date = (Boolean) queryValue;
		if (date == null) {
			return true;
		}
		return eval(date, (T) item);
	}

	protected boolean eval(Boolean value, T item){return false;}
}
