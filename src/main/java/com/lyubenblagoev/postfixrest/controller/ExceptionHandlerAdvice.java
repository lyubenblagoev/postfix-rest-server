package com.lyubenblagoev.postfixrest.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.lyubenblagoev.postfixrest.BadRequestException;
import com.lyubenblagoev.postfixrest.NotFoundException;
import com.lyubenblagoev.postfixrest.service.EntityExistsException;

@ControllerAdvice
public class ExceptionHandlerAdvice {
	
	private static final Logger log = LoggerFactory.getLogger(ExceptionHandlerAdvice.class); 

	@ExceptionHandler(NotFoundException.class)
	private ResponseEntity<ApiErrorResponse> notFoundExceptionHandler(HttpServletRequest request, Exception exception) {
		return handleException(exception, request, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(BadRequestException.class)
	private ResponseEntity<ApiErrorResponse> badRequestExceptionHandler(HttpServletRequest request, Exception exception) {
		return handleException(exception, request, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(EntityExistsException.class)
	private ResponseEntity<ApiErrorResponse> entityExistsExceptionHandler(HttpServletRequest request, Exception exception) {
		return handleException(exception, request, HttpStatus.CONFLICT);
	}

	@ExceptionHandler(BadCredentialsException.class)
	private ResponseEntity<ApiErrorResponse> badCredentialsExceptionHandler(HttpServletRequest request, Exception exception) {
		return handleException(exception, request, HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(Exception.class)
	private ResponseEntity<ApiErrorResponse> exceptionHandler(HttpServletRequest request, Exception exception) {
		return handleException(exception, request, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private ResponseEntity<ApiErrorResponse> handleException(Exception exception, HttpServletRequest request, HttpStatus httpStatus) {
		ApiErrorResponse err = new ApiErrorResponse(exception.getMessage(), request.getRequestURI(), request.getMethod());
		if (log.isInfoEnabled()) {
			log.info(err.toString());
		}
		return new ResponseEntity<>(err, httpStatus);
	}

}
