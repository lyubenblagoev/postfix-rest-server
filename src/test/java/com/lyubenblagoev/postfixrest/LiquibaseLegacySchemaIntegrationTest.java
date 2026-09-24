package com.lyubenblagoev.postfixrest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = "spring.datasource.jdbc-url=jdbc:h2:mem:legacy-mail;MODE=LEGACY;INIT=RUNSCRIPT FROM 'classpath:legacy-pre-audit-schema.sql'")
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LiquibaseLegacySchemaIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldUpgradePreAuditSchemaWithoutLosingExistingData() {
        assertEquals("legacy.example", jdbcTemplate.queryForObject(
                "SELECT name FROM domains WHERE id = 41", String.class));
        assertEquals("legacy-hash", jdbcTemplate.queryForObject(
                "SELECT password FROM accounts WHERE id = 73", String.class));

        assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM audit_logs", Integer.class));
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM audit_log_scope_backfill WHERE id = 1 AND backfill_version >= 2",
                Integer.class));
        jdbcTemplate.execute("SELECT resource_identifier, account_id, domain_id FROM audit_logs");
    }
}
