package com.lyubenblagoev.postfixrest;

public class ApplicaitonRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -4289177181027884657L;

	public ApplicaitonRuntimeException() {
	}

	public ApplicaitonRuntimeException(String message) {
		super(message);
	}

	public ApplicaitonRuntimeException(Throwable cause) {
		super(cause);
	}

	public ApplicaitonRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public ApplicaitonRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
