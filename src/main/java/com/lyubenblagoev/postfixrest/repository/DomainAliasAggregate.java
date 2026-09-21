package com.lyubenblagoev.postfixrest.repository;

public interface DomainAliasAggregate {

    Long getDomainId();

    String getAliasName();

    Long getRecipientCount();

    Long getEnabledRecipients();

    Long getDisabledRecipients();
}
