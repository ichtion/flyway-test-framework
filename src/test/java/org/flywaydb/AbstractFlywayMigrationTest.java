package org.flywaydb;

import org.junit.Before;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;
import javax.sql.DataSource;

public abstract class AbstractFlywayMigrationTest {

    @Inject private DataSource dataSource;
    protected NamedParameterJdbcTemplate jdbcTemplate;

    @Before
    public void before() {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

}
