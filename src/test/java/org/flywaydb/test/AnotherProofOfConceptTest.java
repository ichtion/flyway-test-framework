package org.flywaydb.test;

import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.flywaydb.test.annotation.FlywayMigrationTest;
import org.flywaydb.test.runner.FlywayJUnitRunner;
import org.flywaydb.util.TestUtils;
import org.junit.runner.RunWith;

import static com.google.common.collect.ImmutableMap.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.flywaydb.util.TestUtils.id;

@RunWith(FlywayJUnitRunner.class)
@FlywayMigrationTest(cleanDb = true, migrationVersion = "2", flywayConfiguration = "/flyway.properties")
public class AnotherProofOfConceptTest extends AbstractFlywayMigrationTest {
    private static final String ID = id();
    private static final String NAME = "anoterName";

    @BeforeMigration
    public void insertEmployee() {
        TestUtils.sleepRandomTime();

        jdbcTemplate.update("insert into employee (id, name) values(:id, :name)", of("id", ID, "name", NAME));
    }

    @AfterMigration
    public void assertNameColumnWasRenamedToFirstName() {
        TestUtils.sleepRandomTime();

        String firstname = jdbcTemplate.queryForObject("select firstname from employee where id=:id", of("id", ID), String.class);

        assertThat(firstname).isEqualTo(NAME);
    }

    public AnotherProofOfConceptTest() {
        System.out.println("Constructor for " + this.getClass().getSimpleName());
    }
}
