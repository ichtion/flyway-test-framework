package com.github.ichtion.flywaydb.test;

import com.github.ichtion.flywaydb.test.annotation.AfterMigration;
import com.github.ichtion.flywaydb.test.annotation.BeforeMigration;
import com.github.ichtion.flywaydb.test.annotation.FlywayMigrationTest;
import com.github.ichtion.flywaydb.test.runner.FlywayJUnitMigrationTestRunner;
import com.github.ichtion.flywaydb.util.TestUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.inject.Inject;
import javax.sql.DataSource;

import static com.github.ichtion.flywaydb.util.TestUtils.id;
import static com.google.common.collect.ImmutableMap.of;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(FlywayJUnitMigrationTestRunner.class)
@FlywayMigrationTest(cleanDb = true, migrationVersion = "2_1", flywayConfiguration = "/flyway.properties")
public class AnotherProofOfConceptTest_2 {
    @Inject private static DataSource dataSource;
    private static NamedParameterJdbcTemplate jdbcTemplate;

    private static final String ID = id();
    private static final String NAME = "anotherName";

    @Before
    public static void before() {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @BeforeMigration
    public void insertEmployee() {
        jdbcTemplate.update("insert into employee (id, firstname) values(:id, :name)", of("id", ID, "name", NAME));
    }

    @AfterMigration
    public void assertNameColumnWasRenamedToFirstname() {
        String mainName = jdbcTemplate.queryForObject("select mainname from employee where id=:id", of("id", ID), String.class);

        assertThat(mainName).isEqualTo(NAME);
    }
}