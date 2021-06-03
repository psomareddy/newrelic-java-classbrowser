package com.newrelic.labs.classbrowser;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import com.newrelic.api.agent.NewRelic;

public class ClassAnalyzer {

	private final static String EVENT_TYPE = "ClassBrowser";

	private final static String SINGLE_SPACE = " ";

	private static int gTraceId = 0;
	
	public static void dumpStackTrace() {
		int traceId = gTraceId++;
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (int stackIndex = 0; stackIndex < stack.length; stackIndex++) {
			StackTraceElement s = stack[stackIndex];
			String className = s.getClassName();
			String methodName = s.getMethodName();
			analyzeClass(traceId, stackIndex, className, methodName);
		}
	}
	
	private static void analyzeClass(int traceId, int stackIndex, String className, String methodName) {
		try {			
			Class<?> clazz = Class.forName(className);
			Method[] methods = clazz.getDeclaredMethods();
			for (Method method : methods) {
				Map<String, Object> classAttributes = new HashMap<String, Object>();
				classAttributes.put("packageName", clazz.getPackage().getName());
				classAttributes.put("className", className);
				classAttributes.put("methodName", method.getName());
				if (method.getName().equals(methodName)) {
					classAttributes.put("stackElement", "true");
				} else {
					classAttributes.put("stackElement", "false");
				}
				classAttributes.put("signature", getSignature(method));
				classAttributes.put("stackIndex", stackIndex);
				classAttributes.put("traceId", traceId);
				NewRelic.getAgent().getInsights().recordCustomEvent(EVENT_TYPE, classAttributes);
			}
		} catch (Throwable t) {
			NewRelic.getAgent().getLogger().log(Level.SEVERE, t.getMessage());
		}
	}
	
	private static String getSignature(Method m) {
		String signature;
		try {
			Field gSignature = Method.class.getDeclaredField("signature");
			gSignature.setAccessible(true);
			signature = (String) gSignature.get(m);
			if (signature != null)
				return signature;
		} catch (IllegalAccessException | NoSuchFieldException e) {
			e.printStackTrace();
		}

		StringBuilder sb = new StringBuilder();
		sb.append(Modifier.toString(m.getModifiers()));
		sb.append(SINGLE_SPACE);
		sb.append(m.getReturnType() == void.class ? "void"
				: (signature = Array.newInstance(m.getReturnType(), 0).toString()).substring(1, signature.indexOf('@')));
		sb.append(SINGLE_SPACE);
		sb.append(m.getName());
		sb.append(SINGLE_SPACE);
		sb.append("(");
		for (Class<?> c : m.getParameterTypes())
			sb.append((signature = Array.newInstance(c, 0).toString()).substring(1, signature.indexOf('@')));
		sb.append(')');
				
		return sb.toString();
	}
}
