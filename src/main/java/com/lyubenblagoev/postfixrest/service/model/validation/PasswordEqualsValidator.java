package com.lyubenblagoev.postfixrest.service.model.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.lyubenblagoev.postfixrest.service.model.AccountChangeRequest;

public class PasswordEqualsValidator implements ConstraintValidator<PasswordsMatches, Object> {

	@Override
	public void initialize(PasswordsMatches constraintAnnotation) {
		// Nothing to do here
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		AccountChangeRequest obj = (AccountChangeRequest) value;
		return obj.getPassword() == null || (obj.getConfirmPassword() != null && obj.getPassword().equals(obj.getConfirmPassword()));
	}

}
