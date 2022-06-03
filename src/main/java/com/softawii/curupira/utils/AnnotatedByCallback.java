package com.softawii.curupira.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public interface AnnotatedByCallback<T extends Annotation> {

    void operation(T value, Method method);

}
