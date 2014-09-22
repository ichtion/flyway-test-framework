package org.flywaydb.test.runner;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.test.annotation.FlywayMigrationTest;
import org.flywaydb.test.db.FlywayConfiguration;
import org.flywaydb.test.db.DbMigrator;
import org.junit.runners.model.TestClass;

import static org.flywaydb.test.db.DbMigratorProvider.dbMigratorProvider;

class FlywayTest extends TestClass {

    private final MigrationVersion migrationVersion;
    private final DbMigrator dbMigrator;
    private final String name;
    private final Class<?> underlyingClass;

    public FlywayTest(Class<?> clazz) {
        super(clazz);
        FlywayMigrationTest flywayMigrationTest = clazz.getAnnotation(FlywayMigrationTest.class);
        FlywayConfiguration dataBaseConfiguration = FlywayConfiguration.flywayConfiguration(flywayMigrationTest.flywayConfiguration());
        String versionAsString = flywayMigrationTest.migrationVersion();
        MigrationVersion migrationVersion = MigrationVersion.fromVersion(versionAsString);

        DbMigrator dbMigrator = dbMigratorProvider().provideDbMigratorForConfiguration(dataBaseConfiguration);
        if (!dbMigrator.hasMigration(migrationVersion)) {
            throw new IllegalArgumentException("not existing migration " + versionAsString);
        }
        this.migrationVersion = migrationVersion;
        this.dbMigrator = dbMigrator;
        this.name = clazz.getSimpleName();
        this.underlyingClass = clazz;
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
