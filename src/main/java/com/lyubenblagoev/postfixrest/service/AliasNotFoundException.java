package com.lyubenblagoev.postfixrest.service;


public class AliasNotFoundException extends NotFoundException {
	private static final long serialVersionUID = 1L;

	public AliasNotFoundException() {
	}

	public AliasNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AliasNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public AliasNotFoundException(String message) {
		super(message);
	}

	public AliasNotFoundException(Throwable cause) {
		super(cause);
	}

}
