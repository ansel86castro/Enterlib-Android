package com.enterlib.exceptions;

public class ConnectionFailException extends InvalidOperationException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public ConnectionFailException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ConnectionFailException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

	public ConnectionFailException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public ConnectionFailException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

}
