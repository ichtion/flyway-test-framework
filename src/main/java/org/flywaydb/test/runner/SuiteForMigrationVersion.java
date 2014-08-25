package org.flywaydb.test.runner;

import org.flywaydb.core.api.MigrationVersion;

import java.util.Set;

class SuiteForMigrationVersion {
    private Set<Class<?>> classes;
    private MigrationVersion migrationVersion;

    public SuiteForMigrationVersion(MigrationVersion migrationVersion, Set<Class<?>> classes) {
        this.classes = classes;
        this.migrationVersion = migrationVersion;
    }

    public Set<Class<?>> getClasses() {
        return classes;
    }

    public MigrationVersion getMigrationVersion() {
        return migrationVersion;
    }
}
