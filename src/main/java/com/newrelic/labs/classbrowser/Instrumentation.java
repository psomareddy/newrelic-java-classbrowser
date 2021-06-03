package com.newrelic.labs.classbrowser;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.newrelic.api.agent.weaver.MatchType;
import com.newrelic.api.agent.weaver.NewField;
import com.newrelic.api.agent.weaver.Weave;
import com.newrelic.api.agent.weaver.Weaver;

@Weave(originalName = "javax.servlet.http.HttpServlet", type = MatchType.BaseClass)
public class Instrumentation {

	@NewField
	private static int nrInvocationCount = 0;
	
	protected void service(HttpServletRequest request, HttpServletResponse response) {
		if (nrInvocationCount < 10) {
			nrInvocationCount++;
			ClassAnalyzer.dumpStackTrace();
		}
		if (nrInvocationCount == 1000) {
			nrInvocationCount = 1;
		}
		Weaver.callOriginal();
	}
}
