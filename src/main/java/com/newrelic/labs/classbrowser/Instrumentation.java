package com.newrelic.labs.classbrowser;

import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(originalName = "com.guidewire.pl.system.integration.plugins.PluginInvocationHandler", type = MatchType.BaseClass)
public class Instrumentation {

	@NewField
	private static int nrInvocationCount = 0;
	
	public java.lang.Object invoke(java.lang.Object proxy, java.lang.reflect.Method method, java.lang.Object[] args) {
		if (nrInvocationCount < 10) {
			nrInvocationCount++;
			ClassAnalyzer.dumpStackTrace();
		}
		if (nrInvocationCount == 1000) {
			nrInvocationCount = 1;
		}
		return Weaver.callOriginal();
	}
}
