package com.enterlib.converters;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

import com.enterlib.exceptions.ConversionFailException;

public class TimeConverter implements IStringConverter {

	SimpleDateFormat dftime;

	public TimeConverter() {
		dftime = new SimpleDateFormat("HH:mm", Locale.US);
	}

	public TimeConverter(String timeFormat) {
		dftime = new SimpleDateFormat(timeFormat, Locale.US);
	}

	@Override
	public String getString(Object value) throws ConversionFailException {
		if (value instanceof Date || value == null) {
			if (value == null) {
				return null;
			}
			try {
				Date date = (Date) value;
				return dftime.format(date);
			} catch (Exception e) {
				Log.d("IntegerConverter", "formato no valido :" + value);
				throw new ConversionFailException(e);
			}
		} else {
			Log.d("IntegerConverter", "formato no valido :" + value);
			throw new ConversionFailException();
		}
	}

	@Override
	public Object getObject(String value) throws ConversionFailException {
		// TODO Auto-generated method stub
		return null;
	}

}
