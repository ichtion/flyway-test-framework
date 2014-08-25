package org.flywaydb.test.runner;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.test.annotation.FlywayMigrationTest;
import org.flywaydb.test.db.DbMigrator;

public class FlywayTest {

    private final MigrationVersion migrationVersion;
    private String name;

    private FlywayTest(MigrationVersion migrationVersion) {
        this.migrationVersion = migrationVersion;
    }

    public static FlywayTest create(Class<?> testClass) {
        FlywayMigrationTest flywayMigrationTest = testClass.getAnnotation(FlywayMigrationTest.class);
        DbMigrator dbMigrator = DbMigrator.dbMigratorForConfiguration(flywayMigrationTest.flywayConfiguration());
        String versionAsString = flywayMigrationTest.migrationVersion();
        MigrationVersion version = MigrationVersion.fromVersion(versionAsString);
        if (!dbMigrator.hasMigration(version)) {
            throw new IllegalArgumentException("not existing migration " + versionAsString);
        }
        FlywayTest flywayTest = new FlywayTest(version);
        flywayTest.name = testClass.getSimpleName();

        return flywayTest;
    }

    public MigrationVersion getMigrationVersion() {
        return migrationVersion;
    }

    public String getName() {
        return name;
    }

}
