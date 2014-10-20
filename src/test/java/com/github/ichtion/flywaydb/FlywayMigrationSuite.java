package com.github.ichtion.flywaydb;

import com.github.ichtion.flywaydb.test.annotation.FlywayMigrationTestSuite;
import com.github.ichtion.flywaydb.test.runner.FlywayJUnitMigrationTestSuiteRunner;
import org.junit.runner.RunWith;

@RunWith(FlywayJUnitMigrationTestSuiteRunner.class)
@FlywayMigrationTestSuite(cleanDb = true, flywayConfiguration = "/flyway.properties", packages = {"org.flywaydb.test"})
public class FlywayMigrationSuite  {
}
