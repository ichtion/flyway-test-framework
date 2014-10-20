package com.github.ichtion.flywaydb.test.runner;

import com.github.ichtion.flywaydb.test.annotation.FlywayMigrationTest;
import com.github.ichtion.flywaydb.test.db.DbUtilities;
import com.github.ichtion.flywaydb.test.db.FlywayConfiguration;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.runners.model.TestClass;

import static com.github.ichtion.flywaydb.test.db.DbUtilities.isMigrationAvailable;
import static com.github.ichtion.flywaydb.test.db.FlywayConfiguration.flywayConfiguration;

class FlywayTest extends TestClass {

    private final MigrationVersion migrationVersion;
    private final FlywayConfiguration flywayConfiguration;
    private final String name;
    private final Class<?> underlyingClass;

    public FlywayTest(Class<?> clazz) {
        super(clazz);
        FlywayMigrationTest flywayMigrationTest = clazz.getAnnotation(FlywayMigrationTest.class);
        flywayConfiguration = flywayConfiguration(flywayMigrationTest.flywayConfiguration());
        String versionAsString = flywayMigrationTest.migrationVersion();
        MigrationVersion migrationVersion = MigrationVersion.fromVersion(versionAsString);

        if (!isMigrationAvailable(flywayConfiguration, migrationVersion)) {
            throw new IllegalArgumentException("There is not migration script for " + versionAsString);
        }
        this.migrationVersion = migrationVersion;
        this.name = clazz.getSimpleName();
        this.underlyingClass = clazz;
    }

    public MigrationVersion getMigrationVersion() {
        return migrationVersion;
    }

    public FlywayConfiguration getFlywayConfiguration() {
        return flywayConfiguration;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FlywayTest)) return false;

        FlywayTest that = (FlywayTest) o;

        if (!this.underlyingClass.equals(that.underlyingClass)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return underlyingClass.hashCode();
    }
}
