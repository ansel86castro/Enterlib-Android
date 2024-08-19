package com.enterlib.converters;

import com.enterlib.exceptions.ConversionFailException;

/**
 * Defines methods to convert from an object to its string representation and
 * froms an String to an object
 */
public interface IStringConverter {
	String getString(Object value) throws ConversionFailException;

	Object getObject(String value) throws ConversionFailException;
}