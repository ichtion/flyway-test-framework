package org.flywaydb.test;

import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.flywaydb.test.annotation.FlywayMigrationTest;
import org.flywaydb.test.runner.FlywayJUnitRunner;
import org.flywaydb.util.TestUtils;
import org.junit.Before;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;
import javax.sql.DataSource;

import static com.google.common.collect.ImmutableMap.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.flywaydb.util.TestUtils.id;

@RunWith(FlywayJUnitRunner.class)
@FlywayMigrationTest(cleanDb = true, migrationVersion = "2_1", flywayConfiguration = "/flyway.properties")
public class AnotherProofOfConceptTest_2 {
    @Inject
    private static DataSource dataSource;
    private static NamedParameterJdbcTemplate jdbcTemplate;

    private static final String ID = id();
    private static final String NAME = "anotherName";

    @Before
    public static void beforeClass() {
        System.out.println("Before works");
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

    @After
    public static void afterClass() {
        System.out.println("After works");
    }

    public AnotherProofOfConceptTest_2() {
        System.out.println("Constructor for " + this.getClass().getSimpleName());
    }
}
