package com.enterlib.converters;

import android.view.View;

import com.enterlib.exceptions.ConversionFailException;
import com.enterlib.fields.Field;

/**
 * Defines the contract for a {@link Field} value converter. It is used in model
 * binding to set and get the value of a {@link View} in the {@link Field}
 * class. For example a Field may contains a TextView ,in this case the value of
 * the view is of type string and the Field may be linked to model's field of
 * type {@link Integer}
 * 
 * */
public interface IValueConverter {

	/** convert a source property value to a target property value */
	Object convert(Object value) throws ConversionFailException;

	/**convert a target property value to a source property value */
	Object convertBack(Object value) throws ConversionFailException;
}
