package org.flywaydb.test.db;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.test.util.PropertiesUtils;

import javax.sql.DataSource;

public class DbMigrator {

    private static DbMigrator migrator;
    private MigrationVersion currentMigrationVersion;

    private Flyway flyway;

    // Todo: add DbMigratorProvider for particular configuration
    // (the idea is to have a single instance of migrator per flyway configuration
    // Todo: make it multi-thread proof
    // resolve a problem with two different configurations pointing to the same DB
    public static DbMigrator getMigratorForConfiguration(String flywayConfiguration) {
        if (null != migrator) {
            return migrator;
        }
        migrator = new DbMigrator(flywayConfiguration);
        return migrator;
    }

    private DbMigrator(String flywayConfiguration) {
        flyway = new Flyway();
        flyway.configure(PropertiesUtils.load(flywayConfiguration));
        currentMigrationVersion = computeCurrentVersion();
    }

    private MigrationVersion computeCurrentVersion() {
        if (flywayNotInitialized(flyway)) {
            flyway.init();
        }
        return flyway.info().current().getVersion();
    }

    private boolean flywayNotInitialized(Flyway flyway) {
        return (null == flyway.info().current());
    }

    public boolean migrateToVersion(MigrationVersion migrationVersion) {
        return migrateTo(migrationVersion);
    }

    public boolean migrateToVersionJustBefore(MigrationVersion migrationVersion) {
        MigrationVersion desiredMigrationVersion = getMigrationVersionJustBefore(migrationVersion);
        return migrateTo(desiredMigrationVersion);
    }

    private boolean migrateTo(MigrationVersion desiredMigrationVersion) {
        // todo: clean if needed
        if (currentMigrationVersion.equals(desiredMigrationVersion)) {
            return true;
        }
        if (currentMigrationVersion.compareTo(desiredMigrationVersion) < 0) {
            flyway.setTarget(desiredMigrationVersion);
            flyway.migrate();
            currentMigrationVersion = desiredMigrationVersion;
            return true;
        }
        throw new IllegalStateException(
                "DB already migrated to higher version (current version v" + currentMigrationVersion + ")");
    }

    private MigrationVersion getMigrationVersionJustBefore(MigrationVersion migrationVersion) {
        MigrationVersion previous = MigrationVersion.EMPTY;
        for (MigrationInfo migrationInfo : flyway.info().all()) {
            if (migrationInfo.getVersion().equals(migrationVersion)) {
                return previous;
            } else {
                previous = migrationInfo.getVersion();
            }
        }
        throw new IllegalStateException("previous version not found");
    }


    public DataSource getDataSource() {
        return flyway.getDataSource();
    }

    public boolean hasMigration(MigrationVersion version) {
        MigrationInfoService migrationInfoService = flyway.info();
        for (MigrationInfo migrationInfo : migrationInfoService.all()) {
            if (migrationInfo.getVersion().equals(version)) {
                return true;
            }
        }
        return false;
    }

    public void cleanDb() {
        flyway.clean();
        currentMigrationVersion = MigrationVersion.fromVersion("-1");
    }
}
