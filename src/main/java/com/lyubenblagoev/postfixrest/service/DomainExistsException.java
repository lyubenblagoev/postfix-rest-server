package com.lyubenblagoev.postfixrest.service;

public class DomainExistsException extends BadRequestException {

	private static final long serialVersionUID = 4716674729606712587L;

	public DomainExistsException() {
	}

	public DomainExistsException(String message) {
		super(message);
	}

	public DomainExistsException(Throwable cause) {
		super(cause);
	}

	public DomainExistsException(String message, Throwable cause) {
		super(message, cause);
	}

	public DomainExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
