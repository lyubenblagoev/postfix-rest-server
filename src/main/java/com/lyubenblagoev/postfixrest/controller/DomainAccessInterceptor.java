package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.service.WorkspaceAccessService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;

@Component
public class DomainAccessInterceptor implements HandlerInterceptor {

    private final WorkspaceAccessService workspaceAccessService;

    public DomainAccessInterceptor(WorkspaceAccessService workspaceAccessService) {
        this.workspaceAccessService = workspaceAccessService;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/api/v1/domains/") || path.startsWith("/api/v1/domain-summaries/")) {
            Object pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (pathVariables instanceof Map<?, ?> variables) {
                Map<String, String> values = (Map<String, String>) variables;
                String domainName = values.getOrDefault("domain", values.get("name"));
                if (domainName != null) {
                    workspaceAccessService.assertDomainNameAccess(domainName);
                }
            }
        }
        return true;
    }
}
