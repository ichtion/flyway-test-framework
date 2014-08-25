package org.flywaydb.test.runner;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.test.annotation.FlywayMigrationTest;
import org.flywaydb.test.db.DbMigrator;

public class FlywayTest {

    private final DbMigrator dbMigrator;
    private final MigrationVersion migrationVersion;

    private boolean cleanDb = false;
    private String name;

    private FlywayTest(DbMigrator dbMigrator, MigrationVersion migrationVersion) {
        this.migrationVersion = migrationVersion;
        this.dbMigrator = dbMigrator;
    }

    public static FlywayTest create(Class<?> testClass) {
        FlywayMigrationTest flywayMigrationTest = testClass.getAnnotation(FlywayMigrationTest.class);
        DbMigrator dbMigrator = DbMigrator.getMigratorForConfiguration(flywayMigrationTest.flywayConfiguration());
        String versionAsString = flywayMigrationTest.migrationVersion();
        MigrationVersion version = MigrationVersion.fromVersion(versionAsString);
        if (!dbMigrator.hasMigration(version)) {
            throw new IllegalArgumentException("not existing migration " + versionAsString);
        }
        FlywayTest flywayTest = new FlywayTest(dbMigrator, version);
        flywayTest.name = testClass.getSimpleName();
        flywayTest.cleanDb = flywayMigrationTest.cleanDb();

        return flywayTest;
    }

    public DbMigrator getDbMigrator() {
        return dbMigrator;
    }

    public MigrationVersion getMigrationVersion() {
        return migrationVersion;
    }

    public boolean isCleanDb() {
        return cleanDb;
    }

    public String getName() {
        return name;
    }

}
