package org.flywaydb.test.runner;

import com.google.common.collect.ImmutableSet;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.test.AbstractFlywayMigrationTest;
import org.flywaydb.test.annotation.FlywayMigrationTest;
import org.flywaydb.test.annotation.FlywayMigrationTestSuite;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.reflections.Reflections;

import java.util.*;

public class FlywayJUnitRunner extends ParentRunner<Runner> {
    private SortedMap<MigrationVersion, Set<Class<?>>> flywayClassesPerVersion = new TreeMap<MigrationVersion, Set<Class<?>>>();

    public FlywayJUnitRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        for (Class flywayTestClass : getFlywayTestClass(clazz)) {
            storeFlywayTest(flywayTestClass);
        }
    }

    private void storeFlywayTest(Class<?> flywayTestClass) {
        MigrationVersion migrationVersion = getMigrationVersion(flywayTestClass);
        if (thereAreNoMigrationTestsForGivenMigrationVersion(migrationVersion)) {
            addNewSetWithMigrationTest(flywayTestClass, migrationVersion);
        } else {
            addAnotherFlywayMigrationTest(flywayTestClass, migrationVersion);
        }
    }

    private MigrationVersion getMigrationVersion(Class<?> flywayTestClass) {
        return MigrationVersion.fromVersion(flywayTestClass.getAnnotation(FlywayMigrationTest.class).migrationVersion());
    }

    private void addAnotherFlywayMigrationTest(Class<?> flywayTestClass, MigrationVersion migrationVersion) {
        flywayClassesPerVersion.get(migrationVersion).add(flywayTestClass);
    }

    private void addNewSetWithMigrationTest(Class<?> flywayTest, MigrationVersion migrationVersion) {
        Set<Class<?>> tests = new HashSet<Class<?>>();
        tests.add(flywayTest);
        flywayClassesPerVersion.put(migrationVersion, tests);
    }

    private boolean thereAreNoMigrationTestsForGivenMigrationVersion(MigrationVersion migrationVersion) {
        return flywayClassesPerVersion.get(migrationVersion) == null;
    }

    private Set<Class> getFlywayTestClass(Class<?> clazz) {
        FlywayMigrationTestSuite flywayMigrationTestSuite = clazz.getAnnotation(FlywayMigrationTestSuite.class);
        if (null != flywayMigrationTestSuite) {
            return classesFromSuite(flywayMigrationTestSuite);
        }
        return ImmutableSet.<Class>of(clazz);
    }

    private Set<Class> classesFromSuite(FlywayMigrationTestSuite flywayMigrationTestSuite) {
        Set<Class> classes = new HashSet<Class>();

        for (String packageWithFlywayTests : flywayMigrationTestSuite.packages()) {
            Reflections reflections = new Reflections(packageWithFlywayTests);
            classes.addAll(reflections.getSubTypesOf(AbstractFlywayMigrationTest.class));
        }

        return classes;
    }

    @Override
    protected String getName() {
        return "Migration suite";
    }

    @Override
    protected List<Runner> getChildren() {
        List<Runner> childRunners = new ArrayList<Runner>();

        for (MigrationVersion migrationVersion : flywayClassesPerVersion.keySet()) {
            try {
                SuiteForMigrationVersion suiteForMigrationVersion = new SuiteForMigrationVersion(
                        migrationVersion,
                        flywayClassesPerVersion.get(migrationVersion));
                childRunners.add(new FlywayMigrationSuiteRunner(suiteForMigrationVersion));
            } catch (InitializationError initializationError) {
                throw new RuntimeException(initializationError);
            }
        }

        return childRunners;
    }

    @Override
    protected Description describeChild(Runner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(Runner child, RunNotifier notifier) {
        child.run(notifier);
    }
}
