package com.enterlib.converters;

import android.util.Log;

import com.enterlib.StringUtils;
import com.enterlib.exceptions.ConversionFailException;

/** Implements conversions between String and Integer */
public class IntegerToStringConverter implements IValueConverter {
	boolean nullable;
	
	public IntegerToStringConverter() {
		
	}
	
	public IntegerToStringConverter(boolean nullable){
		this.nullable = nullable;
	}
	
	/**
	 * Convert Integer to String
	 * 
	 * @param value
	 *            must be a Integer or null
	 * @return the String representation, or null if value is null
	 * @throws ConversionFailException
	 *             if value is not an Integer
	 * */
	@Override
	public Object convert(Object value) throws ConversionFailException {
		if (value == null) {
			return null;
		}
		if (value instanceof Integer) {
			return value.toString();
		} else {
			Log.d("IntegerConverter", "invalid format :" + value);
			throw new ConversionFailException("invalid format :" + value);
		}
	}

	/**
	 * Convert String to Integer
	 * 
	 * @param value
	 *            must be a String or null
	 * @return the Integer, or null if value is null or empty
	 * @throws ConversionFailException
	 *             if value is not a String
	 * */
	@Override
	public Object convertBack(Object value) throws ConversionFailException {
		if (value == null) {
			return nullable?null:Integer.valueOf(0);
		}

		if (value instanceof String) {
			String str = (String) value;
			if (StringUtils.isNullOrWhitespace(str)) {
				return nullable? null: Integer.valueOf(0);
			}
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {
				Log.d("IntegerConverter", "invalid format :" + value);
				throw new ConversionFailException(
						"invalid format :" + value, e);
			}
		} else {
			throw new ConversionFailException();
		}
	}

}
