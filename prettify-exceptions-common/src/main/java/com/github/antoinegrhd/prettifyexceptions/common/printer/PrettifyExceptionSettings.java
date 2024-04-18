package com.github.antoinegrhd.prettifyexceptions.common.printer;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrettifyExceptionSettings {
	public enum Failures {
		SILENT,
		MESSAGE,
		FULL
	}
	@JsonProperty("transformPrintStackTrace")
	boolean transformPrintStackTrace = true;
	@JsonProperty("skipOtherPackages")
	boolean isSkipOtherPackages = true;
	@JsonProperty("silentFailures")
	Failures silentFailures = Failures.FULL;
	@JsonProperty("theme")
	PrettifyExceptionTheme theme = new PrettifyExceptionTheme();
	@JsonProperty("highlights")
	Map<String,String> highlights = new HashMap<>();
	@JsonProperty("targets")
	Map<String,String> targets = new HashMap<>();
}
