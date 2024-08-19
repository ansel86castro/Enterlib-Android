package com.enterlib.converters;

import android.util.Log;

import com.enterlib.StringUtils;
import com.enterlib.exceptions.ConversionFailException;

/** Implements conversions between String and Integer */
public class DoubleToStringConverter implements IValueConverter {

	boolean nullable;
	
	public DoubleToStringConverter() {
		// TODO Auto-generated constructor stub
	}
	
	
	
	public DoubleToStringConverter(boolean nullable) {
		super();
		this.nullable = nullable;
	}



	/**
	 * Convert {@link Double} to {@link String}
	 * 
	 * @param value
	 *            must be a Double or null
	 * @return the String representation, or null if value is null
	 * @throws ConversionFailException
	 *             if value is not a Double
	 * */
	@Override
	public Object convert(Object value) throws ConversionFailException {
		if (value == null) {
			return nullable ? null : Double.valueOf(0);
		}
		if (value instanceof Double) {
			return value.toString();
		} else {
			Log.d("DoubleToStringConverter", "invalid format :" + value);
			throw new ConversionFailException("invalid format :" + value);
		}
	}

	/**
	 * Convert {@link String} to {@link Double}
	 * 
	 * @param value
	 *            must be a String or null
	 * @return the {@link Double}, or null if value is null or empty
	 * @throws ConversionFailException
	 *             if value is not a String
	 * */
	@Override
	public Object convertBack(Object value) throws ConversionFailException {
		if (value == null) {
			return null;
		}
		try {
			if (value instanceof String) {
				String strValue = (String) value;
				if (StringUtils.isNullOrWhitespace(strValue)) {
					return nullable? null : Double.valueOf(0.0);
				}

				return Double.valueOf((String) value);
			}
			throw new ConversionFailException();
		} catch (NumberFormatException e) {
			Log.d("DoubleToStringConverter", "invalid format :" + value);
			throw new ConversionFailException("invalid format :" + value, e);
		}
	}
}
