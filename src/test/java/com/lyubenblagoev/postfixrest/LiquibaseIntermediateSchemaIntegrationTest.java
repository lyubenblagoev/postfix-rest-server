package com.lyubenblagoev.postfixrest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "spring.datasource.jdbc-url=jdbc:h2:mem:legacy-audit-mail;MODE=LEGACY;INIT=RUNSCRIPT FROM 'classpath:legacy-audit-scope-v1-schema.sql'")
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LiquibaseIntermediateSchemaIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldUpgradeScopeBackfillVersionOneSchemaAndPopulateResourceIdentifier() {
        assertEquals("legacy.example", jdbcTemplate.queryForObject(
                "SELECT resource_identifier FROM audit_logs WHERE id = 17", String.class));
        assertEquals(41L, jdbcTemplate.queryForObject(
                "SELECT domain_id FROM audit_logs WHERE id = 17", Long.class));
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_log_scope_backfill WHERE id = 1 AND backfill_version >= 2",
                Integer.class));
    }
}
