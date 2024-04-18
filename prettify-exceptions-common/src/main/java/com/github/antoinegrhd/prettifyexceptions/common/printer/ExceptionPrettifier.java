package com.github.antoinegrhd.prettifyexceptions.common.printer;

import lombok.Getter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;


public class ExceptionPrettifier {

	@Getter
	private static PrettifyExceptionSettings settings = new PrettifyExceptionSettings();
	private static FramePrinter framePrinter = new FramePrinter(settings);
	private static LinePrinter linePrinter = new LinePrinter(framePrinter, settings);

	public static void setSettings(PrettifyExceptionSettings settings) {
		ExceptionPrettifier.settings = settings;
		ExceptionPrettifier.framePrinter = new FramePrinter(settings);
		ExceptionPrettifier.linePrinter = new LinePrinter(framePrinter, settings);
	}

	public static void printStackTrace(Throwable throwable) {
		String res = ExceptionPrettifier.prettify(throwable);
		System.err.print(res);
	}

	public static String prettify(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));
		String exStr = stringWriter.toString() ;
		return ExceptionPrettifier.prettify(exStr);
	}

	public static String prettify(String exStr) {
		if(exStr == null || exStr.isEmpty()) {
			return exStr;
		}

		List<String> stack = List.of(exStr.split("\n"));
		PrintState printState = new PrintState();
		HashMap<String, String> traces = ExceptionPrettifier.getLogTrace();
		printState.setLogTraces(traces);

		List<String> result = new ArrayList<>();
		for (int i = 0; i < stack.size(); i++) {
			String str = stack.get(i);
			printState.setNextLine(i+1 < stack.size() ? stack.get(i+1) : null);
			result.addAll(linePrinter.printLine(str, printState).stream().filter(Objects::nonNull).toList());
		}

		String stackTrace = String.join("\n", result);
		exStr = "\n" + stackTrace + "\n\n";

		return exStr;
	}

	private static HashMap<String, String> getLogTrace() {
		Exception trace = new Exception("getLogTrace");
		HashMap<String, String> traces = new LinkedHashMap<>();
		StackTraceElement[] logStackStrace = trace.getStackTrace();
		for(int i = 1; i < logStackStrace.length; i++) {
			String steStr = logStackStrace[i].toString();
			String previousSteStr = logStackStrace[i-1].toString();
			String mess = "Logged with '" + logStackStrace[i-1].getMethodName() + "' on line " + logStackStrace[i].getLineNumber();
			traces.putIfAbsent(steStr.split("\\(")[0],mess);
		}
		return traces;
	}
}
