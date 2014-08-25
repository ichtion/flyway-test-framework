package org.flywaydb.test;

import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.flywaydb.test.annotation.FlywayMigrationTest;
import org.flywaydb.test.runner.FlywayJUnitRunner;
import org.junit.runner.RunWith;

import static com.google.common.collect.ImmutableMap.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.flywaydb.util.TestUtils.id;

@RunWith(FlywayJUnitRunner.class)
@FlywayMigrationTest(cleanDb = true, migrationVersion = "2_1", flywayConfiguration = "/flyway.properties")
public class ProofOfConceptTest_2 extends AbstractFlywayMigrationTest {

    private static final String ID = id();
    private static final String NAME = "name";

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
