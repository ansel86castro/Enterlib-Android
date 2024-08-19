package com.enterlib.serialization;

import com.enterlib.data.IFactory;
import com.enterlib.exceptions.InvalidOperationException;

/**
 * Serialize an object into a string format
 * 
 * @author ansel
 *
 */
public interface IStringSerializer {

	/**
	 * Convert object into a string representation
	 * 
	 * @param object
	 *            The object to serialize
	 * @return
	 * @throws InvalidOperationException
	 */
	String serialize(Object object) throws InvalidOperationException;

	/**
	 * Desarialize the string {@code value} into a object of Class {@code type}
	 * 
	 * @param type
	 * @param value
	 * @return
	 * @throws InvalidOperationException
	 *             if the String can not be deserialize into an object of class
	 *             {@code type}
	 */
	Object deserialize(Class<?> type, String value)
			throws InvalidOperationException;

}