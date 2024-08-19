package com.enterlib.exceptions;

/**
 * Error that is thrown during types conversion due to incompatible types.
 **/
public class ConversionFailException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public ConversionFailException() {
		super("invalid format");

	}

	public ConversionFailException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

	public ConversionFailException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public ConversionFailException(Throwable throwable) {
		super("invalid format", throwable);
		// TODO Auto-generated constructor stub
	}

}
