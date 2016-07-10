package com.lyubenblagoev.postfixrest.service;

public class DomainNotFoundException extends NotFoundException {

	private static final long serialVersionUID = -391889659507867579L;

	public DomainNotFoundException() {
	}

	public DomainNotFoundException(String message) {
		super(message);
	}

	public DomainNotFoundException(Throwable cause) {
		super(cause);
	}

	public DomainNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public DomainNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
