package org.flywaydb.test;

import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.flywaydb.test.annotation.FlywayMigrationTest;
import org.flywaydb.test.runner.FlywayJUnitRunner;
import org.flywaydb.util.TestUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;
import javax.sql.DataSource;

import static com.google.common.collect.ImmutableMap.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.flywaydb.util.TestUtils.id;

@RunWith(FlywayJUnitRunner.class)
@FlywayMigrationTest(cleanDb = true, migrationVersion = "2_1", flywayConfiguration = "/flyway.properties")
public class ProofOfConceptTest_2 {
    @Inject
    private DataSource dataSource;
    protected NamedParameterJdbcTemplate jdbcTemplate;


    private static final String ID = id();
    private static final String NAME = "name";

    @Before
    public void before() {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @BeforeMigration
    public void insertEmployee() {
        TestUtils.sleepRandomTime();

        jdbcTemplate.update("insert into employee (id, firstname) values(:id, :name)", of("id", ID, "name", NAME));
    }

    @AfterMigration
    public void assertNameColumnWasRenamedToFirstname() {
        TestUtils.sleepRandomTime();

        String mainName = jdbcTemplate.queryForObject("select mainname from employee where id=:id", of("id", ID), String.class);

        assertThat(mainName).isEqualTo(NAME);
    }

    public ProofOfConceptTest_2() {
        System.out.println("Constructor for " + this.getClass().getSimpleName());
    }
}
