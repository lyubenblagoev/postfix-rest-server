package com.lyubenblagoev.postfixrest.service;

public class EntityExistsException extends RuntimeException {

	private static final long serialVersionUID = 4861593009857124251L;

	public EntityExistsException() {
	}

	public EntityExistsException(String message) {
		super(message);
	}

	public EntityExistsException(Throwable cause) {
		super(cause);
	}

	public EntityExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public EntityExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
