package org.flywaydb.test.runner;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.flywaydb.test.db.DbUtilities;
import org.flywaydb.test.db.FlywayConfiguration;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static org.flywaydb.test.db.DbUtilities.migrateDbToVersionJustBefore;

class MigrateToVersionRule implements TestRule {
    private final MigrationVersion migrationVersion;
    private final FlywayConfiguration flywayConfiguration;

    public MigrateToVersionRule(MigrationVersion migrationVersion, FlywayConfiguration configuration) {
        this.migrationVersion = migrationVersion;
        this.flywayConfiguration = configuration;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (description.getAnnotation(BeforeMigration.class) != null) {
                    migrateDbToVersionJustBefore(flywayConfiguration, migrationVersion);
                } else if (description.getAnnotation(AfterMigration.class) != null) {
                    DbUtilities.migrateDbToVersion(flywayConfiguration, migrationVersion);
                }
                base.evaluate();
            }
        };
    }
}
