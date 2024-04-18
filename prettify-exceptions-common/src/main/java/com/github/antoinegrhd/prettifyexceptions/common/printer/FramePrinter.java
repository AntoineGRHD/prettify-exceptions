package com.github.antoinegrhd.prettifyexceptions.common.printer;

import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
class FramePrinter {

	private PrettifyExceptionSettings settings;

	enum FrameType {
		CALLER,
		FIRST,
		HIGHLIGHTED,
		OTHER,
	}

	private FrameType getFrameType(String line, PrintState printState) {
		if(printState.getCallerPackage() == null) {
			Optional<String> callerPackage = printState.getLogTraces().keySet().stream().filter(line::contains).findFirst();
			if(callerPackage.isPresent()) {
				printState.setCallerPackage(callerPackage.get());
				printState.setNextIsFirstTrace(false);
				return FrameType.CALLER;
			}
		}
		if(printState.isNextIsFirstTrace()) {
			printState.setNextIsFirstTrace(false);
			return FrameType.FIRST;
		}
		if(settings.getHighlights().keySet().stream().anyMatch(line::contains)) {
			return FrameType.HIGHLIGHTED;
		}
		return FrameType.OTHER;
	}

	protected String printFrame(String line, PrintState printState) {
		FrameType frameType = getFrameType(line, printState);
		String formattedLine = formatString(line);

		return switch (frameType) {
			case CALLER -> printCaller(formattedLine, printState);
			case FIRST -> printFirst(formattedLine, printState);
			case HIGHLIGHTED -> printHighlighted(formattedLine, settings.getTheme().getOther());
			case OTHER -> printOther(formattedLine, printState);
		};
	}

	private String formatString(String line) {
		int locationIndex = line.lastIndexOf("(");
		if(locationIndex < 0) return line;
		int methodIndex = line.substring(0,locationIndex).lastIndexOf(".");
		if(methodIndex < 0) return line;
		int classIndex =  line.substring(0,methodIndex).lastIndexOf(".");
		if(classIndex < 0) return line;

		String atPackage = line.substring(0, classIndex+1);
		String className = line.substring(classIndex+1, methodIndex+1);
		String methodName = line.substring(methodIndex+1, locationIndex);
		String suffix = line.substring(locationIndex);

		return "\033[38;5;" + settings.getTheme().getAtPackage() + "m" + atPackage
				+ "\033[38;5;%sm" + className
				+ "\033[1m" + methodName
				+ "\033[38;5;" + settings.getTheme().getSuffix()+"m\033[22m\033[3m" + suffix
				+ "\033[0m";
	}

	private String printCaller(String line, PrintState printState) {
		String strLog = "";

		if(printState.getLogTraces().containsKey(printState.getCallerPackage())) {
			strLog = " \033[38;5;"+settings.getTheme().getLog()+"m\uD83D\uDCC4 " + printState.getLogTraces().get(printState.getCallerPackage()) + "\033[0m";
		}
		return printHighlighted(line, settings.getTheme().getCaller()) + strLog;
	}

	private String printFirst(String line, PrintState printState) {
		return printHighlighted(line, settings.getTheme().getFirst());
	}

	private String printHighlighted(String line, String defaultColor) {
		return settings.getHighlights().keySet()
				.stream()
				.map(highlight -> "\tat " + highlight)
				.filter(line::startsWith)
				.sorted((v1, v2) -> v2.split("\\.").length - v1.split("\\.").length)
				.findFirst()
				.map(key -> settings.getHighlights().get(key))
				.map(color -> String.format(line, color))
				.orElse(String.format(line, defaultColor));
	}

	private String printOther(String line, PrintState printState) {
		if (settings.isSkipOtherPackages() && printState.getNextLine() != null && printState.getNextLine().startsWith("\tat ")) {
			printState.incrementSkippedLines();
			return null;
		} else {
			return String.format(line, settings.getTheme().getOther());
		}
	}
}
