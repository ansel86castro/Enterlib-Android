package com.enterlib.converters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

import com.enterlib.R;
import com.enterlib.exceptions.ConversionFailException;
import com.enterlib.fields.DatePickerButtonField;

/**
 * Converts Dates to String and String to Dates. This class is used with the
 * {@link DatePickerButtonField} to convert from the view's value to the model
 * field's value when the field's value is a String
 */
public class DateToStringConverter implements IValueConverter {
	SimpleDateFormat df;

	/** Converts dates format of dd/MM/yyyy by default */
	public DateToStringConverter() {
		df = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
		df.setLenient(false);
	}

	public DateToStringConverter(String format) {
		df = new SimpleDateFormat(format);
		df.setLenient(false);
	}

	public DateToStringConverter(String format, Locale locale) {
		df = new SimpleDateFormat(format, locale);
		df.setLenient(false);
	}

	public String getString(Date date) {
		if (date == null) {
			return null;
		}
		return df.format(date);
	}

	public Date getDate(String value) {
		if (value == null) {
			return null;
		}
		try {
			return df.parse(value);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			Log.e("DateConverter", "getDate", e); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}

	/**
	 * Convert a {@link String} from a {@link Date}
	 *
	 * @param value
	 *            Must be of type Date or null
	 * @return null if the value is null or empty
	 * @throws ConversionFailException
	 *             if the value is not of type {@link Date}
	 * */
	@Override
	public Object convert(Object value) throws ConversionFailException {
		if (value instanceof Date || value == null) {
			if (value == null) {
				return null;
			}
			try {
				Date date = (Date) value;
				return df.format(date);
			} catch (Exception e) {
				Log.d("IntegerConverter", "Invalid Format" + value); //$NON-NLS-1$ //$NON-NLS-2$
				throw new ConversionFailException("Invalid Format" + value, e); //$NON-NLS-1$
			}
		} else {
			Log.d("IntegerConverter", "Invalid Format" + value); //$NON-NLS-1$ //$NON-NLS-2$
			throw new ConversionFailException();
		}
	}

	/**
	 * Converts a String to Date
	 * 
	 * @param the
	 *            value to convert. Must be of type String or null
	 * @return null if the value is null or empty
	 * @throws ConversionFailException
	 *             if the value is not a valid date format
	 * */
	@Override
	public Object convertBack(Object value) throws ConversionFailException {
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
			Log.d("IntegerConverter","Invalid Format" + value); //$NON-NLS-1$ //$NON-NLS-2$
			throw new ConversionFailException();
		}
	}
}
