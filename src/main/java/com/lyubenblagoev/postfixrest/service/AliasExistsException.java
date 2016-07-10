package com.lyubenblagoev.postfixrest.service;

public class AliasExistsException extends BadRequestException {
	private static final long serialVersionUID = 6667677710797994817L;

	public AliasExistsException() {
		super();
	}

	public AliasExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public AliasExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public AliasExistsException(String message) {
		super(message);
	}

	public AliasExistsException(Throwable cause) {
		super(cause);
	}

	
}
