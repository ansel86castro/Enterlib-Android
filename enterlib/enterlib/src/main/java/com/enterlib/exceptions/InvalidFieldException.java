package com.enterlib.exceptions;

/**
 * {@link Exception} that is thrown during Json serialization or deserialization.
 * This indicates that a object's field can not be serialized or deserialize due
 * to an IllegalAccessException or IllegalArgumentException
 */
public class InvalidFieldException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public InvalidFieldException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public InvalidFieldException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

	public InvalidFieldException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public InvalidFieldException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

}
