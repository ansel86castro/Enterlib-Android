package com.enterlib.filtering;

import android.content.Context;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.Switch;

import com.enterlib.converters.IValueConverter;
import com.enterlib.exceptions.ConversionFailException;
import com.enterlib.fields.CompoundButtonField;

public class SwitchCondition<T> extends FilterCondition {
	private Switch view;
	private String textOn;
	private String textOff;

	public SwitchCondition(Context context, String queryName, String queryHint,
			IValueConverter converter, String textOn, String textOff,
			boolean onSet) {
		super(queryName, queryHint, null);

		this.textOn = textOn;
		this.textOff = textOff;
		view = new Switch(context);
        LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		view.setLayoutParams(layoutParams);
		view.setTextOn(textOn);
		view.setTextOff(textOff);
		view.setChecked(onSet);

		setField(new CompoundButtonField(view, "FilterCondition" + queryHint));

		if (converter == null) {
			converter = new IValueConverter() {
				@Override
				public Object convertBack(Object value)
						throws ConversionFailException {
					return view.isChecked() ? SwitchCondition.this.textOn
							: SwitchCondition.this.textOff;
				}

				@Override
				public Object convert(Object value)
						throws ConversionFailException {
					// NOT USED
					return null;
				}
			};
		}

		this.converter = converter;

	}

	public SwitchCondition(Context context, String queryHint, String textOn,
			String textOff, boolean onSet) {
		this(context, null, queryHint, null, textOn, textOff, onSet);
	}

	public SwitchCondition(Context context, String queryName, String queryHint,
			String textOn, String textOff, boolean onSet) {
		this(context, queryName, queryHint, null, textOn, textOff, onSet);
	}

	public SwitchCondition(Context context, String queryName, String queryHint,
			boolean onSet) {
		this(context, queryName, queryHint, null, null, null, onSet);
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

	protected boolean eval(Boolean value, T item) {
		return false;
	}
}
