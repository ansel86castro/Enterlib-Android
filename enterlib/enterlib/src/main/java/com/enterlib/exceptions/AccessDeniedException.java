package com.enterlib.exceptions;

/**
 * {@link Exception} that is thrown when an access to a resource or a operation
 * is denied
 */
public class AccessDeniedException extends Exception {

	/**
	 *
	 */
	private static final long serialVersionUID = 3546227906166947373L;

	public AccessDeniedException() {

	}

	public AccessDeniedException(String detailMessage) {
		super(detailMessage);

	}

	public AccessDeniedException(Throwable throwable) {
		super(throwable);

	}

	public AccessDeniedException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);

	}

}
