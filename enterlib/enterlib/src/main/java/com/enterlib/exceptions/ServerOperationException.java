package com.enterlib.exceptions;

public class ServerOperationException extends InvalidOperationException {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public ServerOperationException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ServerOperationException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

	public ServerOperationException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public ServerOperationException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

}
