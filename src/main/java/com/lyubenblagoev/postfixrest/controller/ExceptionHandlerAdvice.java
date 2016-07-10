package com.lyubenblagoev.postfixrest.controller;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.lyubenblagoev.postfixrest.service.BadRequestException;
import com.lyubenblagoev.postfixrest.service.NotFoundException;

@ControllerAdvice
public class ExceptionHandlerAdvice {
	
	private static final Logger log = Logger.getLogger(ExceptionHandlerAdvice.class);

	@ExceptionHandler(NotFoundException.class)
	private ResponseEntity<ErrorInfo> notFoundHandler(HttpServletRequest request, Exception exception) {
		return handleException(exception, request, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(BadRequestException.class)
	private ResponseEntity<ErrorInfo> badRequestHandler(HttpServletRequest request, Exception exception) {
		return handleException(exception, request, HttpStatus.BAD_REQUEST);
	}
	
	private ResponseEntity<ErrorInfo> handleException(Exception exception, HttpServletRequest request, HttpStatus httpStatus) {
		ErrorInfo err = new ErrorInfo(exception.getMessage(), request.getRequestURI(), request.getMethod());
		log.info(err.toString());
		return new ResponseEntity<>(err, httpStatus);
	}

}
