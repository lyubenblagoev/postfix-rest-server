package com.lyubenblagoev.postfixrest.configuration;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.interceptor.CustomizableTraceInterceptor;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@Configuration
public class RequestLoggingFilterConfig {

	@Bean
	public CommonsRequestLoggingFilter requestLoggingFilter() {
		CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
		filter.setIncludeQueryString(true);
		filter.setIncludePayload(true);
		filter.setIncludeClientInfo(true);
		filter.setMaxPayloadLength(500);
		return filter;
	}
	
	@Bean
	public CustomizableTraceInterceptor customizableTraceInterceptor() {
	    return new CustomizableTraceInterceptor();
	}
	
	@Bean
	public Advisor traceAdvisor() {
	    AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
	    pointcut.setExpression("execution(public * com.lyubenblagoev.postfixrest..*.*(..))");
	    return new DefaultPointcutAdvisor(pointcut, customizableTraceInterceptor());
	}

}
