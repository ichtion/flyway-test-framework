package org.flywaydb.test.runner;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.flywaydb.test.db.DbMigrator;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

class MigrateToVersionRule implements TestRule {
    private final MigrationVersion migrationVersion;
    private final DbMigrator dbMigrator;

    public MigrateToVersionRule(MigrationVersion migrationVersion, DbMigrator dbMigrator) {
        this.migrationVersion = migrationVersion;
        this.dbMigrator = dbMigrator;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (description.getAnnotation(BeforeMigration.class) != null) {
                    dbMigrator.migrateToVersionJustBefore(migrationVersion);
                } else if (description.getAnnotation(AfterMigration.class) != null) {
                    dbMigrator.migrateToVersion(migrationVersion);
                }
                base.evaluate();
            }
        };
    }
}
