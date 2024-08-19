package com.enterlib.converters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

import com.enterlib.exceptions.ConversionFailException;

/**
 * Converts Dates to String and String to Dates.
 */

public class DateConverter implements IStringConverter {
	SimpleDateFormat df;
	boolean removeT = true;
	
	public DateConverter() {
		df = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		df.setLenient(false);
	}

	public DateConverter(String format) {
		df = new SimpleDateFormat(format, Locale.US);
		df.setLenient(false);
	}
	
	public DateConverter(String format, boolean removeT) {
		this(format);
		this.removeT = removeT;
	}

	public DateConverter(String format, Locale locale) {
		df = new SimpleDateFormat(format, locale);
		df.setLenient(false);
	}

	public DateConverter(String format, Locale locale, boolean removeT){
		this(format, locale);
		this.removeT = removeT;
	}
	/**
	 * returns a string in the format dd/MM/yyyy for default constructor
	 * 
	 * @throws ConversionFailException
	 */
	@Override
	public String getString(Object value) throws ConversionFailException {
		if (value instanceof Date || value == null) {
			if (value == null) {
				return null;
			}
			try {
				Date date = (Date) value;
				return df.format(date);
			} catch (Exception e) {
				Log.d("DateConverter", "invalid format :" + value);
				throw new ConversionFailException(e);
			}
		} else {
			Log.d("DateConverter", "invalid format :" + value);
			throw new ConversionFailException();
		}
	}

	public String getString(Date date) {
		if (date == null) {
			return null;
		}
		return df.format(date);
	}

	public Date getDate(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		try {
			if(removeT && value.length() > 10 && value.charAt(10)=='T'){
				value = value.replace('T', ' ');				
			}
			return df.parse(value);
		} catch (ParseException e) {
			Log.d("DateConverter", "invalid format :" + value);
			throw new ConversionFailException();
		}
	}

	/**
	 * value in the format dd/MM/yyyy for defaultConstructor
	 * 
	 * @throws ConversionFailException
	 */
	@Override
	public Object getObject(String value) throws ConversionFailException {
		return getDate(value);
	}
}
