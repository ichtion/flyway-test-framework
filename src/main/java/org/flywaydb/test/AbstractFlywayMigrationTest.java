package org.flywaydb.test;

import org.flywaydb.test.db.DbMigrator;
import org.junit.Before;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;

public abstract class AbstractFlywayMigrationTest {

    @Inject private DbMigrator dbMigrator;
    protected NamedParameterJdbcTemplate jdbcTemplate;

    @Before
    public void before() {
        jdbcTemplate = new NamedParameterJdbcTemplate(dbMigrator.getDataSource());
    }

}
