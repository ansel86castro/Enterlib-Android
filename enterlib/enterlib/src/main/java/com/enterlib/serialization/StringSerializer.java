package com.enterlib.serialization;

import java.util.HashMap;

import android.util.Log;

import com.enterlib.converters.IStringConverter;
import com.enterlib.exceptions.ConversionFailException;
import com.enterlib.exceptions.InvalidOperationException;

/**
 * The base class for the {@link IStringSerializer}
 * 
 * @author ansel
 *
 */
public abstract class StringSerializer implements IStringSerializer {
	HashMap<Class<?>, IStringConverter> converters = new HashMap<Class<?>, IStringConverter>();

	/**
	 * Register a custom Type converter for using with instance of Class
	 * {@code type}
	 * 
	 * @param type
	 * @param converter
	 */
	public void registerConverted(Class<?> type, IStringConverter converter) {
		converters.put(type, converter);
	}

	/**
	 * Returns true if it contains a {@link IStringConverter} for the given
	 * Class
	 * 
	 * @param type
	 * @return
	 */
	protected boolean hasConverterFor(Class<?> type) {
		return converters.containsKey(type);
	}

	/**
	 * Returns the {@link IStringConverter} for the given Class or null
	 * 
	 * @param type
	 * @return
	 */
	protected IStringConverter getConverterFor(Class<?> type) {
		IStringConverter conv = converters.get(type);
		return conv;
	}

	protected String convertToString(Object value, Class<?> type)
			throws InvalidOperationException {
		IStringConverter conv = converters.get(type);
		return convertToString(value, conv);
	}

	protected String convertToString(Object value, IStringConverter converter)
			throws InvalidOperationException {
		try {
			return converter.getString(value);
		} catch (ConversionFailException e) {
			Log.d(getClass().getName(), e.getMessage());
			throw new InvalidOperationException(e.getMessage(), e);
		}
	}

	protected Object convertBack(String value, Class<?> type)
			throws InvalidOperationException {
		IStringConverter conv = converters.get(type);
		return convertBack(value, conv);
	}

	protected Object convertBack(String value, IStringConverter converter)
			throws InvalidOperationException {
		try {
			return converter.getObject(value);
		} catch (ConversionFailException e) {
			Log.d(getClass().getName(), e.getMessage());
			throw new InvalidOperationException(e.getMessage(), e);
		}
	}

	/**
	 * return the null representation of the {@link StringSerializer} by default
	 * it is "null"
	 * 
	 * @return
	 */
	protected String getNullString() {
		return "null";
	}
}
