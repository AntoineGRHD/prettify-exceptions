package com.github.antoinegrhd.prettifyexceptions.common.printer;

import lombok.AllArgsConstructor;

import java.util.LinkedList;
import java.util.List;

@AllArgsConstructor
class LinePrinter {

	private FramePrinter framePrinter;
	private PrettifyExceptionSettings settings;

	enum LineType {
		MESSAGE,
		EMPTY,
		COMMON,
		FRAME,
	}

	private LineType getLineType(String line) {
		if(line.isBlank()) {
			return LineType.EMPTY;
		}
		if(line.startsWith("\t...")) {
			return LineType.COMMON;
		}
		if(!line.startsWith("\tat ")) {
			return LineType.MESSAGE;
		}

		return LineType.FRAME;
	}
	
	public List<String> printLine(String line, PrintState printState) {
		List<String> result = new LinkedList<>();
		LineType lineType = getLineType(line);

		String lineToPrint = switch (lineType) {
			case EMPTY -> null;
			case COMMON -> printCommon(line, printState);
			case MESSAGE -> printMessage(line, printState);
			case FRAME ->  framePrinter.printFrame(line, printState);
		};

		if(lineToPrint != null) {
			String skipped = printSkipped(printState);
			if(skipped != null) {
				result.add(skipped);
			}
			result.add(lineToPrint);
		}
		return result;
	}

	private String printCommon(String line, PrintState printState){
		var andSkippedFrames = "";
		if (printState.getSkippedLines() > 0) {
			andSkippedFrames = " and " + printState.getSkippedLines() + " skipped frames";
			printState.resetSkippedLines();
		}
		return "\033[38;5;" + settings.getTheme().getCommon() + ";3m" + line + andSkippedFrames + "\033[0m";
	}

	private String printSkipped(PrintState printState) {
		if (printState.getSkippedLines() > 0) {
			String line = "\033[38;5;" + settings.getTheme().getSkipped() + ";3m\t... " + printState.getSkippedLines() + " skipped frames\033[0m";
			printState.resetSkippedLines();
			return line;
		}
		return null;
	}

	private String printMessage(String line, PrintState printState) {
		printState.setNextIsFirstTrace(true);
		boolean isCause = line.startsWith("Caused by: ");
		String prefix = "";
		if(isCause) {
			line = line.substring("Caused by: ".length());
			prefix = "Caused by: ";
		}
		int separatorIndex =  line.indexOf(":");
		String atPackageCause = line.substring(0, separatorIndex+1);
		String message = line.substring(separatorIndex+1);
		return "\033[38;5;"+settings.getTheme().getMessage()+";3m" + prefix + "\033[23m" + atPackageCause + "\033[1m" + message + " \033[0m";
	}
}
