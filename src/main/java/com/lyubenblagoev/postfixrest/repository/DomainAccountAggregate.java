package com.lyubenblagoev.postfixrest.repository;

public interface DomainAccountAggregate {

    Long getDomainId();

    Long getTotal();

    Long getEnabled();

    Long getDisabled();
}
