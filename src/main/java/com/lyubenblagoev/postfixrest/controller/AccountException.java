package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.service.BadRequestException;

public class AccountException extends BadRequestException {

	private static final long serialVersionUID = 2975332953232178651L;

	public AccountException() {
	}

	public AccountException(String message) {
		super(message);
	}

	public AccountException(Throwable cause) {
		super(cause);
	}

	public AccountException(String message, Throwable cause) {
		super(message, cause);
	}

	public AccountException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
