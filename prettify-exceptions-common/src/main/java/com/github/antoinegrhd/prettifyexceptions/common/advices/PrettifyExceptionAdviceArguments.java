package com.github.antoinegrhd.prettifyexceptions.common.advices;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.PARAMETER)
@interface PrettifyExceptionAdviceArguments {}
