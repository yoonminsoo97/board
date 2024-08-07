package com.board.domain.member.validator.annotation;

import com.board.domain.member.validator.UsernameValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UsernameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Username {

    String requiredMessage() default "아이디를 입력해 주세요.";

    String regexpMessage() default "5~15자의 영문 소문자, 숫자를 사용해 주세요.";

    String regexp() default "^[a-z0-9]{5,15}$";

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
