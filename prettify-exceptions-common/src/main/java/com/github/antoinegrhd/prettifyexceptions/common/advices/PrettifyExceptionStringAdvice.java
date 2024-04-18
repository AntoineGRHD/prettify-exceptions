package com.github.antoinegrhd.prettifyexceptions.common.advices;

import net.bytebuddy.asm.Advice;

import java.io.PrintWriter;
import java.io.StringWriter;

class PrettifyExceptionStringAdvice {

	@Advice.OnMethodExit
	public static void prettifyException(
			@Advice.Return(readOnly = false) String exStr,
			@PrettifyExceptionAdviceArguments String adviceArgument
	) {
		if(exStr == null || exStr.isEmpty()) return;

		try {
			Object result = Class.forName("com.github.antoinegrhd.prettifyexceptions.common.printer.ExceptionPrettifier", false, ClassLoader.getSystemClassLoader())
					.getMethod("prettify", String.class)
					.invoke(null, exStr);
			if(result instanceof String resultStr) {
				exStr = resultStr;
			}
		} catch (Throwable t) {
			String toPrint = switch (adviceArgument) {
				case "FULL" -> {
					StringWriter stringWriter = new StringWriter();
					t.printStackTrace(new PrintWriter(stringWriter));
					yield "\nError while prettifying the exception :\n" + stringWriter.toString();
				}
				case "MESSAGE" -> "\nError while prettifying the exception : " + t.getMessage() + "\n";
				default -> "";
			};
			exStr = exStr + toPrint;
		}
	}
}