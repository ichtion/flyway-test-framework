package org.flywaydb.test;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.test.annotation.FlywayMigrationTest;
import org.flywaydb.test.db.DbMigrator;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;

public abstract class AbstractFlywayMigrationTest {

    @Inject private DbMigrator dbMigrator;
    protected NamedParameterJdbcTemplate jdbcTemplate;

    @Before
    public void before() {
        jdbcTemplate = new NamedParameterJdbcTemplate(dbMigrator.getDataSource());
    }

    @Test
    public void migrate() {
        FlywayMigrationTest flywayMigrationTest = this.getClass().getAnnotation(FlywayMigrationTest.class);
        dbMigrator.migrateToVersion(MigrationVersion.fromVersion(flywayMigrationTest.migrationVersion()));
    }
}
