package com.enterlib.converters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.util.Log;

import com.enterlib.exceptions.ConversionFailException;

public class CalendarConverter implements IStringConverter {

	SimpleDateFormat df;
	boolean removeT;
	
	public CalendarConverter() {
		df = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
		df.setLenient(false);
	}

	public CalendarConverter(String format) {
		df = new SimpleDateFormat(format, Locale.US);
		df.setLenient(false);
	}

	public CalendarConverter(String format, Locale locale) {
		df = new SimpleDateFormat(format, locale);
		df.setLenient(false);
	}
	
	public CalendarConverter(String format, boolean removeT) {
		this(format);
		this.removeT =removeT;
	}

	public CalendarConverter(String format, Locale locale, boolean removeT) {
		this(format,locale);
		this.removeT = removeT;
	}

	/**
	 * returns a string in the format dd/MM/yyyy for default constructor
	 * 
	 * @throws ConversionFailException
	 */
	@Override
	public String getString(Object value) throws ConversionFailException {
		if (value instanceof Calendar || value == null) {
			if (value == null) {
				return null;
			}
			try {
				Calendar date = (Calendar) value;
				return df.format(date.getTime());
			} catch (Exception e) {
				Log.d("CalendarConverter", "invalid format :" + value);
				throw new ConversionFailException(e);
			}
		} else {
			Log.d("CalendarConverter", "invalid format :" + value);
			throw new ConversionFailException();
		}
	}

	public String getString(Calendar date) {
		if (date == null) {
			return null;
		}
		return df.format(date.getTime());
	}

	public Calendar getDate(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		try {
			if(removeT && value.length() > 10 && value.charAt(10)=='T'){
				value = value.replace('T', ' ');
			}
			
			Calendar c = Calendar.getInstance(Locale.getDefault());						
			c.setTime(df.parse(value));
			return c;
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
