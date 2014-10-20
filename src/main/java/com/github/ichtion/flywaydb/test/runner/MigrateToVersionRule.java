package com.github.ichtion.flywaydb.test.runner;

import com.github.ichtion.flywaydb.test.annotation.AfterMigration;
import com.github.ichtion.flywaydb.test.annotation.BeforeMigration;
import com.github.ichtion.flywaydb.test.db.DbUtilities;
import com.github.ichtion.flywaydb.test.db.FlywayConfiguration;
import org.flywaydb.core.api.MigrationVersion;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static com.github.ichtion.flywaydb.test.db.DbUtilities.migrateDbToVersion;
import static com.github.ichtion.flywaydb.test.db.DbUtilities.migrateDbToVersionJustBefore;

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
                    migrateDbToVersion(flywayConfiguration, migrationVersion);
                }
                base.evaluate();
            }
        };
    }
}
