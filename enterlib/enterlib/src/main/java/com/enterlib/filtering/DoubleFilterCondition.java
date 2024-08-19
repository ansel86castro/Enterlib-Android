package com.enterlib.filtering;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.enterlib.R;
import com.enterlib.StringUtils;
import com.enterlib.converters.IValueConverter;
import com.enterlib.exceptions.ConversionFailException;
import com.enterlib.fields.TextViewField;

public class DoubleFilterCondition<T> extends FilterCondition
		implements IValueConverter {
	static final String LogTag = DoubleFilterCondition.class.getName();

	public DoubleFilterCondition(Context context, String queryName,
			String queryHint) {
		super(queryName, queryHint);

		LayoutInflater inflater = LayoutInflater.from(context);
		TextView view = (TextView) inflater.inflate(
				R.layout.layout_filter_double, null);

		 TextViewField field = new TextViewField(view, "DoubleFilterCondition"
				+ queryHint);
		field.setValueConverter(this);
		this.filterOp = FilterOperators.EQUALS;
		
		setField(field);
	}

	public DoubleFilterCondition(Context context, String queryName,
			String queryHint, int filterOp) {
		this(context, queryName, queryHint);

		this.filterOp = filterOp;
	}

	public DoubleFilterCondition(Context context, String queryHint) {
		this(context, null, queryHint);

	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean eval(Object item) {
		Double value = (Double) queryValue;
		if (value == null) {
			return true;
		}
		return eval(value, (T) item);
	}

	protected boolean eval(Double value, T item){return false;}

	@Override
	public Object convert(Object value) throws ConversionFailException {
		if (value == null) {
			return null;
		}
		if (value instanceof Double) {
			return value.toString();
		} else {
			Log.d(LogTag, "invalid format :" + value);
			throw new ConversionFailException("invalid format :" + value);
		}
	}

	@Override
	public Object convertBack(Object value) throws ConversionFailException {
		if (value == null) {
			return null;
		}
		try {
			if (value instanceof String) {
				String strValue = (String) value;
				if (StringUtils.isNullOrWhitespace(strValue)) {
					return null;
				}
				return Double.valueOf((String) value);
			} else {
				throw new ConversionFailException(
						"The parameter value is not String");
			}
		} catch (NumberFormatException e) {
			Log.d(LogTag, "invalid format :" + value);
			throw new ConversionFailException("invalid format :" + value, e);
		}
	}
}
