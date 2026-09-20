package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.BadRequestException;
import com.lyubenblagoev.postfixrest.entity.AuditEntityType;
import com.lyubenblagoev.postfixrest.entity.AuditScope;
import com.lyubenblagoev.postfixrest.service.AuditLogService;
import com.lyubenblagoev.postfixrest.service.model.AuditLogResource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
@Validated
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLogResource> query(
            @RequestParam(required = false) AuditEntityType entityType,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) AuditScope scope,
            @RequestParam(required = false) @Min(1) Long scopeId,
            @RequestParam(defaultValue = "50") @Min(1) @Max(100) int limit) {
        if ((scope == null) != (scopeId == null)) {
            throw new BadRequestException("scope and scopeId must be provided together");
        }
        if (scope != null) {
            return auditLogService.find(scope, scopeId, limit);
        }
        return auditLogService.find(entityType, entityId, limit);
    }
}
