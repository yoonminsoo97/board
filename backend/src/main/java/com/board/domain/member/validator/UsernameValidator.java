package com.board.domain.member.validator;

import com.board.domain.member.validator.annotation.Username;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import org.springframework.util.StringUtils;

public class UsernameValidator implements ConstraintValidator<Username, String> {

    private String requiredMessage;
    private String regexpMessage;
    private String regexp;

    @Override
    public void initialize(Username constraintAnnotation) {
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
