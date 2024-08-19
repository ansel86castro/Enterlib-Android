package com.enterlib.exceptions;

import com.enterlib.annotations.DataMember;

/**
 * {@link Exception} that is thrown during Json serialization or deserialization.
 * This indicates that a object's property (a method with get or set prefix) can
 * not be serialized or deserialize due to an IllegalAccessException or
 * IllegalArgumentException. Also the method must be marked with a
 * {@link DataMember} annotation
 */
public class InvalidPropertyException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public InvalidPropertyException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public InvalidPropertyException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

	public InvalidPropertyException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public InvalidPropertyException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

}
