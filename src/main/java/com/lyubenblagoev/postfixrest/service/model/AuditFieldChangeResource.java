package com.lyubenblagoev.postfixrest.service.model;

public class AuditFieldChangeResource {

    private String field;
    private String oldValue;
    private String newValue;

    public AuditFieldChangeResource() {
    }

    public AuditFieldChangeResource(String field, String oldValue, String newValue) {
        this.field = field;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getField() {
        return field;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }
}
