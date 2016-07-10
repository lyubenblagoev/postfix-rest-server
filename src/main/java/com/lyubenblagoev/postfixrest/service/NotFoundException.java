package com.lyubenblagoev.postfixrest.service;

import com.lyubenblagoev.postfixrest.ApplicaitonRuntimeException;

public class NotFoundException extends ApplicaitonRuntimeException {

	private static final long serialVersionUID = -1967579868614870552L;

	public NotFoundException() {
	}

	public NotFoundException(String message) {
		super(message);
	}

	public NotFoundException(Throwable cause) {
		super(cause);
	}

	public NotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public NotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
