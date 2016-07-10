package com.lyubenblagoev.postfixrest.service;

public class AccountExistsException extends BadRequestException {

	private static final long serialVersionUID = 4861593009857124251L;

	public AccountExistsException() {
	}

	public AccountExistsException(String message) {
		super(message);
	}

	public AccountExistsException(Throwable cause) {
		super(cause);
	}

	public AccountExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public AccountExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
