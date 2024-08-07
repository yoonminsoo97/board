package com.board.domain.member.validator;

import com.board.domain.member.validator.annotation.Password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.util.StringUtils;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    private String requiredMessage;
    private String regexpMessage;
    private String regexp;

    @Override
    public void initialize(Password constraintAnnotation) {
        requiredMessage = constraintAnnotation.requiredMessage();
        regexpMessage = constraintAnnotation.regexpMessage();
        regexp = constraintAnnotation.regexp();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        if (!StringUtils.hasLength(value)) {
            context.buildConstraintViolationWithTemplate(requiredMessage)
                    .addConstraintViolation();
            return false;
        }
        context.buildConstraintViolationWithTemplate(regexpMessage)
                .addConstraintViolation();
        return value.matches(regexp);
    }

}
