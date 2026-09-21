package com.lyubenblagoev.postfixrest.service.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.lyubenblagoev.postfixrest.entity.Domain;

import java.util.Date;

public class DomainSummaryResource {

    private Long id;
    private String name;
    private boolean enabled;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date created;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private Date updated;

    private AccountSummary accounts;
    private AliasSummary aliases;

    public static DomainSummaryResource fromDomain(Domain domain, AccountSummary accounts, AliasSummary aliases) {
        DomainSummaryResource resource = new DomainSummaryResource();
        resource.id = domain.getId();
        resource.name = domain.getName();
        resource.enabled = domain.isEnabled();
        resource.created = domain.getCreated();
        resource.updated = domain.getUpdated();
        resource.accounts = accounts;
        resource.aliases = aliases;
        return resource;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Date getCreated() {
        return created;
    }

    public Date getUpdated() {
        return updated;
    }

    public AccountSummary getAccounts() {
        return accounts;
    }

    public AliasSummary getAliases() {
        return aliases;
    }

    public static class AccountSummary {

        private final long total;
        private final long enabled;
        private final long disabled;

        public AccountSummary(long total, long enabled, long disabled) {
            this.total = total;
            this.enabled = enabled;
            this.disabled = disabled;
        }

        public long getTotal() {
            return total;
        }

        public long getEnabled() {
            return enabled;
        }

        public long getDisabled() {
            return disabled;
        }
    }

    public static class AliasSummary {

        private final long total;
        private final long healthy;
        private final long needsReview;
        private final long paused;

        public AliasSummary(long total, long healthy, long needsReview, long paused) {
            this.total = total;
            this.healthy = healthy;
            this.needsReview = needsReview;
            this.paused = paused;
        }

        public long getTotal() {
            return total;
        }

        public long getHealthy() {
            return healthy;
        }

        public long getNeedsReview() {
            return needsReview;
        }

        public long getPaused() {
            return paused;
        }
    }
}
