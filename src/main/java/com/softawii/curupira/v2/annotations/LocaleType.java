package com.softawii.curupira.v2.annotations;

import com.softawii.curupira.v2.enums.LocaleTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface LocaleType {
    LocaleTypeEnum value() default LocaleTypeEnum.USER;
}
