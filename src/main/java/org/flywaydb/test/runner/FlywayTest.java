package org.flywaydb.test.runner;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.test.annotation.FlywayMigrationTest;
import org.flywaydb.test.db.FlywayConfiguration;
import org.flywaydb.test.db.DbMigrator;

import static org.flywaydb.test.db.DbMigratorProvider.dbMigratorProvider;

class FlywayTest {

    private final MigrationVersion migrationVersion;
    private final DbMigrator dbMigrator;
    private String name;

    private FlywayTest(MigrationVersion migrationVersion, DbMigrator dbMigrator) {
        this.migrationVersion = migrationVersion;
        this.dbMigrator = dbMigrator;
    }

    public static FlywayTest create(Class<?> testClass) {
        FlywayMigrationTest flywayMigrationTest = testClass.getAnnotation(FlywayMigrationTest.class);
        FlywayConfiguration dataBaseConfiguration = FlywayConfiguration.flywayConfiguration(flywayMigrationTest.flywayConfiguration());
        String versionAsString = flywayMigrationTest.migrationVersion();
        MigrationVersion version = MigrationVersion.fromVersion(versionAsString);

        DbMigrator dbMigrator = dbMigratorProvider().provideDbMigratorForConfiguration(dataBaseConfiguration);
        if (!dbMigrator.hasMigration(version)) {
            throw new IllegalArgumentException("not existing migration " + versionAsString);
        }
        FlywayTest flywayTest = new FlywayTest(version, dbMigrator);
        flywayTest.name = testClass.getSimpleName();

        return flywayTest;
    }

    public MigrationVersion getMigrationVersion() {
        return migrationVersion;
    }

    public DbMigrator getDbMigrator() {
        return dbMigrator;
    }

    public String getName() {
        return name;
    }

}
