package org.flywaydb;

import org.flywaydb.test.annotation.FlywayMigrationTestSuite;
import org.flywaydb.test.runner.FlywayJUnitMigrationTestSuiteRunner;
import org.junit.runner.RunWith;

@RunWith(FlywayJUnitMigrationTestSuiteRunner.class)
@FlywayMigrationTestSuite(cleanDb = true, flywayConfiguration = "/flyway.properties", packages = {"org.flywaydb.test"})
public class FlywayMigrationSuite  {
}
