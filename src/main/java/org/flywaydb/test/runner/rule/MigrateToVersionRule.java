package org.flywaydb.test.runner.rule;

import org.flywaydb.test.annotation.AfterMigration;
import org.flywaydb.test.annotation.BeforeMigration;
import org.flywaydb.test.db.DbMigrator;
import org.flywaydb.test.runner.FlywayTest;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class MigrateToVersionRule implements TestRule {
    private final FlywayTest flywayTest;
    private final DbMigrator dbMigrator;

    public MigrateToVersionRule(FlywayTest flywayTest, DbMigrator dbMigrator) {
        this.flywayTest = flywayTest;
        this.dbMigrator = dbMigrator;
    }

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (description.getAnnotation(BeforeMigration.class) != null) {
                    dbMigrator.migrateToVersionJustBefore(flywayTest.getMigrationVersion());
                } else if (description.getAnnotation(AfterMigration.class) != null) {
                    dbMigrator.migrateToVersion(flywayTest.getMigrationVersion());
                }
                base.evaluate();
            }
        };
    }
}
