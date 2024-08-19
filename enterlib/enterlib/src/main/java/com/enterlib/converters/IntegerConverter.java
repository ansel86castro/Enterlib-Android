package com.enterlib.converters;

import android.util.Log;

import com.enterlib.exceptions.ConversionFailException;

public class IntegerConverter implements IStringConverter {

	@Override
	public String getString(Object value) throws ConversionFailException {
		if (value == null) {
			return null;
		}
		if (value instanceof Integer) {
			return value.toString();
		} else {
			Log.d("IntegerConverter", "formato no valido :" + value);
			throw new ConversionFailException();
		}
	}

	@Override
	public Object getObject(String value) throws ConversionFailException {
		if (value == null) {
			return null;
		}

		try {
			return Integer.decode(value);
		} catch (NumberFormatException e) {
			Log.d("IntegerConverter", "formato no valido :" + value);
			throw new ConversionFailException(e);
		}
	}

}
