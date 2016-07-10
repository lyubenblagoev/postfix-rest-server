package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.ApplicaitonRuntimeException;

public class BadRequestException extends ApplicaitonRuntimeException {

	private static final long serialVersionUID = 540536621916319350L;

	public BadRequestException() {
	}

	public BadRequestException(String message) {
		super(message);
	}

	public BadRequestException(Throwable cause) {
		super(cause);
	}

	public BadRequestException(String message, Throwable cause) {
		super(message, cause);
	}

	public BadRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
