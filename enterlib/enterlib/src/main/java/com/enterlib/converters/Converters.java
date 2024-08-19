package com.enterlib.converters;

import java.util.Locale;

import com.enterlib.data.PropertyMap;
import com.enterlib.exceptions.ConversionFailException;

/** Contains common converters */
public final class Converters {
	public static final IntegerToStringConverter IntegerToStringConverter = new IntegerToStringConverter();
	public static final DoubleToStringConverter DoubleToStringConverter = new DoubleToStringConverter();
	public static final DateToStringConverter DateToStringConverter = new DateToStringConverter();
	public static final DateToStringConverter TimeToStringConverter = new DateToStringConverter(
			"HH:mm", Locale.US);

	public static final StringToDateConverter StringToDateConverter = new StringToDateConverter();
	public static final StringToDateConverter StringToTimeConverter = new StringToDateConverter(
			"HH:mm", Locale.US);

	public static final DateConverter DateConverter = new DateConverter();
	public static final TimeConverter TimeConverter = new TimeConverter();
	
	
}
