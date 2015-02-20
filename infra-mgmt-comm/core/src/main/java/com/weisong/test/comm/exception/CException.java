package com.weisong.test.comm.exception;

public class CException extends Exception {

	private static final long serialVersionUID = 1L;

	public CException() {
		super();
	}

	public CException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CException(String message, Throwable cause) {
		super(message, cause);
	}

	public CException(String message) {
		super(message);
	}

	public CException(Throwable cause) {
		super(cause);
	}
}
