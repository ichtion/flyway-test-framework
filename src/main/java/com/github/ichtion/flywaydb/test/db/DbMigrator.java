package com.github.ichtion.flywaydb.test.db;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationInfoService;
import org.flywaydb.core.api.MigrationVersion;

import javax.sql.DataSource;

class DbMigrator {

    private MigrationVersion currentMigrationVersion;
    private Flyway flyway;

    DbMigrator(Flyway flyway) {
        this.flyway = flyway;
        currentMigrationVersion = computeCurrentVersion();
    }

    private MigrationVersion computeCurrentVersion() {
        return flyway.info().current().getVersion();
    }

    public boolean migrateToVersion(MigrationVersion migrationVersion) {
        return migrateTo(migrationVersion);
    }

    boolean migrateToVersionJustBefore(MigrationVersion migrationVersion) {
        MigrationVersion desiredMigrationVersion = getMigrationVersionJustBefore(migrationVersion);
        return migrateTo(desiredMigrationVersion);
    }

    private boolean migrateTo(MigrationVersion desiredMigrationVersion) {
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

    DataSource getDataSource() {
        return flyway.getDataSource();
    }

    boolean hasMigration(MigrationVersion version) {
        MigrationInfoService migrationInfoService = flyway.info();
        for (MigrationInfo migrationInfo : migrationInfoService.all()) {
            if (migrationInfo.getVersion().equals(version)) {
                return true;
            }
        }
        return false;
    }

    void cleanDb() {
        flyway.clean();
        currentMigrationVersion = MigrationVersion.fromVersion("-1");
    }
}
