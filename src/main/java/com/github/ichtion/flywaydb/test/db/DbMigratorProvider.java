package com.github.ichtion.flywaydb.test.db;

import java.util.HashMap;
import java.util.Map;

class DbMigratorProvider {
    private static DbMigratorProvider dbMigratorProvider = new DbMigratorProvider();
    private DbMigratorFactory dbMigratorFactory = new DbMigratorFactory();
    private Map<FlywayConfiguration, DbMigrator> dbMigratorForFlywayConfiguration = new HashMap<FlywayConfiguration, DbMigrator>();

    public static DbMigratorProvider dbMigratorProvider() {
        return dbMigratorProvider;
    }

    public DbMigrator provideDbMigratorForConfiguration(FlywayConfiguration flywayConfiguration) {
        if (!dbMigratorForFlywayConfiguration.containsKey(flywayConfiguration)) {
            dbMigratorForFlywayConfiguration.put(flywayConfiguration, dbMigratorFactory.create(flywayConfiguration));
        }

        return dbMigratorForFlywayConfiguration.get(flywayConfiguration);
    }
}