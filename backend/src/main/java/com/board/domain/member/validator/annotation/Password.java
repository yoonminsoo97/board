package com.board.domain.member.validator.annotation;

import com.board.domain.member.validator.PasswordValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {

    String requiredMessage() default "비밀번호를 입력해 주세요.";

    String regexpMessage() default "8~16자의 영문 대/소문자, 숫자, 특수문자를 사용해 주세요.";

    String regexp() default "^[A-Za-z0-9`\\-=\\\\\\[\\];',./~!@#$%^&*()_+|{}:\"<>?]{8,16}$";

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
