package com.lyubenblagoev.postfixrest.controller;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

public class ControllerUtils {
	
	private ControllerUtils() {
		// Hide the implicit public constructor
	}

	public static String getError(BindingResult result) {
		String err = null;
		if (result.hasErrors()) {
			ObjectError e = result.getAllErrors().get(0);
			if (e instanceof FieldError) {
				err = String.format("Field '%s' %s", ((FieldError)e).getField(), e.getDefaultMessage());
			} else {
				err = e.getDefaultMessage();
			}
		}
		return err;
	}

}
