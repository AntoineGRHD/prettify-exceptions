package com.github.antoinegrhd.prettifyexceptions.common.advices;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

class PrettifyPrintStackTraceAdvice {

	@Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
	public static Object prettifyException(
			@Advice.This Object invokedObject,
			@Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args,
			@PrettifyExceptionAdviceArguments String adviceArgument
	) {

		if(args.length > 0 ) return null;
		if(invokedObject instanceof Exception throwable) {
			try {
				Class.forName("com.github.antoinegrhd.prettifyexceptions.common.printer.ExceptionPrettifier", false, ClassLoader.getSystemClassLoader())
						.getMethod("printStackTrace", Throwable.class)
						.invoke(null, throwable);
				return true;
			} catch (Throwable t) {
				throwable.printStackTrace(System.err);

				switch (adviceArgument) {
					case "FULL" -> {
						System.err.println("\nError while prettifying the exception :\n");
						t.printStackTrace(System.err);
					}
					case "MESSAGE" -> System.err.println("Error while prettifying the exception : " + t.getMessage());
				}
			}
		}
		return null;
	}

}