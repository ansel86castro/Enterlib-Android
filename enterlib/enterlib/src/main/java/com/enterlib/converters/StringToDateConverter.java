package com.enterlib.converters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

import com.enterlib.exceptions.ConversionFailException;

/** Similar to {@link DateToStringConverter} */
public class StringToDateConverter implements IValueConverter {

	SimpleDateFormat df;

	public StringToDateConverter() {
		df = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		df.setLenient(false);
	}

	public StringToDateConverter(String format) {
		df = new SimpleDateFormat(format, Locale.US);
		df.setLenient(false);
	}

	public StringToDateConverter(String format, Locale locale) {
		df = new SimpleDateFormat(format, locale);
		df.setLenient(false);
	}

	/**
	 * converts a String to Date
	 * 
	 * @param the
	 *            value to convert. Must be of type String or null
	 * @throws ConversionFailException
	 *             if value is not a valid date string
	 * */
	@Override
	public Object convert(Object value) throws ConversionFailException {
		if (value == null) {
			return null;
		}
		try {
			if (value instanceof String) {
				String str = (String) value;
				if (str.isEmpty()) {
					return null;
				}
				return df.parse(str);
			}
			throw new ConversionFailException();
		} catch (ParseException e) {
			Log.d("IntegerConverter", "formato no valido :" + value);
			throw new ConversionFailException();
		}
	}

	/**
	 * Convert a String from a Date
	 *
	 * @param value
	 *            Must be of type Date or null
	 * @throws ConversionFailException
	 *             if value is not of Type {@link Date}
	 * */
	@Override
	public Object convertBack(Object value) throws ConversionFailException {
		if (value instanceof Date || value == null) {
			if (value == null) {
				return null;
			}
			try {
				Date date = (Date) value;
				return df.format(date);
			} catch (Exception e) {
				Log.d("IntegerConverter", "formato no valido :" + value);
				throw new ConversionFailException(e);
			}
		} else {
			Log.d("IntegerConverter", "formato no valido :" + value);
			throw new ConversionFailException();
		}
	}

}
