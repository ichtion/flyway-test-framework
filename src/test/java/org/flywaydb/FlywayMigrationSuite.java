package org.flywaydb;

import org.flywaydb.test.annotation.FlywayMigrationTestSuite;
import org.flywaydb.test.runner.FlywayJUnitRunner;
import org.junit.runner.RunWith;

@RunWith(FlywayJUnitRunner.class)
@FlywayMigrationTestSuite(packages = {"org.flywaydb.test"})
public class FlywayMigrationSuite  {
}
