package com.github.antoinegrhd.prettifyexceptions.common.advices;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.antoinegrhd.prettifyexceptions.common.printer.ExceptionPrettifier;
import com.github.antoinegrhd.prettifyexceptions.common.printer.PrettifyExceptionSettings;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Optional;

public class PrettifyExceptionInstaller {

	public static void install(Class<?> contextualClass, String resourceName) {
		Instrumentation instrumentation = ByteBuddyAgent.install();
		PrettifyExceptionInstaller.install(contextualClass, resourceName, instrumentation);
	}

	public static void install(Class<?> contextualClass, String resourceName, Instrumentation instrumentation) {
		try {
			installInternal(contextualClass, resourceName, instrumentation);
		} catch (Throwable t) {
			System.err.println("Prettify-Exceptions installation failed.");
			t.printStackTrace(System.err);
		}
	}

	private static void installInternal(
			Class<?> contextualClass,
			String resourceName,
			Instrumentation instrumentation
	) throws Exception {

		try {
			URL resource = Optional.ofNullable(contextualClass.getClassLoader().getResource(resourceName)).orElseThrow();
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			JsonNode configFile = mapper.readTree(resource);
			JsonNode prettifyExceptionsConfig = configFile.path("prettify-exceptions");
			PrettifyExceptionSettings settings = mapper.treeToValue(prettifyExceptionsConfig, PrettifyExceptionSettings.class);
			ExceptionPrettifier.setSettings(settings);
		} catch (Exception e) {
			System.err.println("Exception concerning prettify-exceptions options.");
			throw e;
		}

		if(ExceptionPrettifier.getSettings().isTransformPrintStackTrace()) {
			PrettifyExceptionInstaller.transformPrintStackTrace(instrumentation);
		}
		if(!ExceptionPrettifier.getSettings().getTargets().isEmpty()) {
			PrettifyExceptionInstaller.transformStackTraceOutputs(instrumentation);
		}

	}

	private static void transformPrintStackTrace(Instrumentation instrumentation) {
		new AgentBuilder.Default()
				.disableClassFormatChanges()
				.ignore(ElementMatchers.none())
				.type(ElementMatchers.nameContains("java.lang.Throwable"))
				.transform(new AgentBuilder.Transformer() {
					@Override
					public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, ProtectionDomain protectionDomain) {
						System.out.println("Transform " + typeDescription.getName());
						return builder.visit(Advice.withCustomMapping().bind(PrettifyExceptionAdviceArguments.class, ExceptionPrettifier.getSettings().getSilentFailures().toString()).to(PrettifyPrintStackTraceAdvice.class).on(ElementMatchers.named("printStackTrace")));
					}
				})
				.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
				.installOn(instrumentation);
	}

	private static void transformStackTraceOutputs(Instrumentation instrumentation) {
		new AgentBuilder.Default()
				.disableClassFormatChanges()
				.ignore(ElementMatchers.none())
				.type(PrettifyExceptionInstaller.buildClassNameMatcher(ExceptionPrettifier.getSettings().getTargets().keySet().stream().toList()))
				.transform(new AgentBuilder.Transformer() {
					@Override
					public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, ProtectionDomain protectionDomain) {
						System.out.println("Transform " + typeDescription.getName());
						return builder.visit(Advice.withCustomMapping().bind(PrettifyExceptionAdviceArguments.class, ExceptionPrettifier.getSettings().getSilentFailures().toString()).to(PrettifyExceptionStringAdvice.class).on(ElementMatchers.named(ExceptionPrettifier.getSettings().getTargets().get(typeDescription.getName()))));
					}
				})
				.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
				.installOn(instrumentation);
	}

	private static ElementMatcher.Junction<NamedElement> buildClassNameMatcher(List<String> classNames) {
		if(classNames.isEmpty()) return ElementMatchers.none();
		return ElementMatchers.nameContains(classNames.get(0)).or(buildClassNameMatcher(classNames.subList(1, classNames.size())));
	}


}

