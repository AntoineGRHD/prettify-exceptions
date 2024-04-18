package com.github.antoinegrhd.prettifyexceptions.agent;

import com.github.antoinegrhd.prettifyexceptions.common.advices.PrettifyExceptionInstaller;
import java.lang.instrument.Instrumentation;

public class PrettifyExceptionAgent {
	public static void premain(String agentArgs, Instrumentation instrumentation) {
		System.out.println("Premain PrettifyExceptionAgent : "+agentArgs );
		try {
			String[] argArray = agentArgs.split(":");
			String className = argArray[0];
			String resourceName = argArray[1];
			Class<?> contextualClass = Class.forName(className);
			PrettifyExceptionInstaller.install(contextualClass, resourceName, instrumentation);
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
	}
}
