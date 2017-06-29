package com.lyubenblagoev.postfixrest.service.model.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordEqualsValidator.class)
public @interface PasswordsMatches {
	
	String message() default "Passwords do not match";

	Class<?>[] groups() default {};
	
	Class<? extends Payload>[] payload() default {};
    
}
