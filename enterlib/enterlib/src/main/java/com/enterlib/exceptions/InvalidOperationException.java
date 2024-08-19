package com.enterlib.exceptions;

/**
 * The exception that is thrown when a method call is invalid for the object's
 * current state.
 */
public class InvalidOperationException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public InvalidOperationException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public InvalidOperationException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

	public InvalidOperationException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public InvalidOperationException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

}
