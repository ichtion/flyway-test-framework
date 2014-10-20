package com.github.ichtion.flywaydb.test.db;

import org.flywaydb.core.Flyway;

class DbMigratorFactory {
    private FlywayFactory flywayFactory = new FlywayFactory();

    DbMigrator create(FlywayConfiguration configuration) {
        return new DbMigrator(flywayFactory.createInitializedFlyway(configuration));
    }

    static private class FlywayFactory {
        Flyway createInitializedFlyway(FlywayConfiguration configuration) {
            Flyway flyway = new Flyway();
            flyway.configure(configuration);
            if (flywayNotInitialized(flyway)) {
                flyway.init();
            }
            return flyway;
        }

        private static boolean flywayNotInitialized(Flyway flyway) {
            return (null == flyway.info().current());
        }

    }
}


