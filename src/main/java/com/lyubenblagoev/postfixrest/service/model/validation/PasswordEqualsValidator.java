package com.lyubenblagoev.postfixrest.service.model.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.lyubenblagoev.postfixrest.service.model.AccountChangeRequest;
import com.lyubenblagoev.postfixrest.service.model.PasswordConfirmable;

import java.util.Objects;

public class PasswordEqualsValidator implements ConstraintValidator<PasswordsMatches, PasswordConfirmable> {

	@Override
	public void initialize(PasswordsMatches constraintAnnotation) {
		// Nothing to do here
	}

	@Override
	public boolean isValid(PasswordConfirmable value, ConstraintValidatorContext context) {
	    return Objects.equals(value.getPassword(), value.getPasswordConfirmation());
	}

}
