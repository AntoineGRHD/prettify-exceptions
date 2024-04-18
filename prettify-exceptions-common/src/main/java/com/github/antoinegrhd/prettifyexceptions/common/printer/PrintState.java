package com.github.antoinegrhd.prettifyexceptions.common.printer;

import lombok.*;

import java.util.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
class PrintState {
	private boolean nextIsFirstTrace = false;
	private String nextLine = null;
	private int skippedLines = 0;
	private String callerPackage = null;
	private HashMap<String, String> logTraces = new HashMap<>();

	public void incrementSkippedLines() {
		skippedLines = skippedLines + 1;
	}

	public void resetSkippedLines() {
		skippedLines = 0;
	}
}