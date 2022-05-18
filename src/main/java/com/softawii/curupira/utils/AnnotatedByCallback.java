package com.softawii.curupira.utils;

import net.dv8tion.jda.api.interactions.commands.Command;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

public interface AnnotatedByCallback<T extends Annotation> {

    void operation(T value, Method method);

}
