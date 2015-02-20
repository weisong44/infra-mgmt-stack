package com.weisong.test.comm.exception;


public class CTimeoutException extends CException {

	private static final long serialVersionUID = 1L;

	public CTimeoutException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

	public CTimeoutException(String message) {
		super(message);
	}

	public CTimeoutException(Throwable cause) {
		super(cause);
	}
}
