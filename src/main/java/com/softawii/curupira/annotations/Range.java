package com.softawii.curupira.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Range {

    Argument value();
    int      min();
    int      max();
    int      steps() default 1;
}
