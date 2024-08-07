package com.board.domain.member.validator.annotation;

import com.board.domain.member.validator.NicknameValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NicknameValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Nickname {

    String requiredMessage() default "닉네임을 입력해 주세요.";

    String regexpMessage() default "5~10자의 영문 소문자, 숫자를 사용해 주세요.";

    String regexp() default "^[가-힣a-z0-9]{5,10}$";

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
