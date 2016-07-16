package com.lyubenblagoev.postfixrest.service;

public class BccNotFoundException extends NotFoundException {

	private static final long serialVersionUID = 7594716358939713569L;

	public BccNotFoundException() {
	}

	public BccNotFoundException(String message) {
		super(message);
	}

	public BccNotFoundException(Throwable cause) {
		super(cause);
	}

	public BccNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public BccNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
