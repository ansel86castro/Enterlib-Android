package com.enterlib.exceptions;

/** The exception that is thrown when a parameter's is invalid for the operation */
public class InvalidParameterException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public InvalidParameterException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public InvalidParameterException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

	public InvalidParameterException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public InvalidParameterException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

}
