package com.github.ichtion.flywaydb.test.db;

import org.flywaydb.core.api.MigrationVersion;

import javax.sql.DataSource;

import static com.github.ichtion.flywaydb.test.db.DbMigratorProvider.dbMigratorProvider;

public class DbUtilities {
    public static void cleanDb(FlywayConfiguration configuration) {
        dbMigratorProvider().provideDbMigratorForConfiguration(configuration).cleanDb();
    }

    public static void migrateDbToVersion(FlywayConfiguration configuration, MigrationVersion version) {
        dbMigratorProvider().provideDbMigratorForConfiguration(configuration).migrateToVersion(version);
    }

    public static void migrateDbToVersionJustBefore(FlywayConfiguration configuration, MigrationVersion version) {
        dbMigratorProvider().provideDbMigratorForConfiguration(configuration).migrateToVersionJustBefore(version);
    }

    public static boolean isMigrationAvailable(FlywayConfiguration configuration, MigrationVersion version) {
        return dbMigratorProvider().provideDbMigratorForConfiguration(configuration).hasMigration(version);
    }

    public static DataSource getDataSource(FlywayConfiguration configuration) {
        return dbMigratorProvider().provideDbMigratorForConfiguration(configuration).getDataSource();
    }
}
