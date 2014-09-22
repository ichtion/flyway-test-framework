package org.flywaydb.test.runner;

import org.flywaydb.test.runner.rule.MigrateToVersionRule;
import org.junit.rules.TestRule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;

import static org.flywaydb.test.runner.TestInstanceProvider.testInstanceProvider;

abstract class FlywayParticularMigrationTestRunner extends BlockJUnit4ClassRunner {

    private FlywayTest flywayTest;

    FlywayParticularMigrationTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
        flywayTest = new FlywayTest(klass);
    }

    @Override
    protected String getName() {
        return flywayTest.getName();
    }

    @Override
    protected Object createTest() throws Exception {
       return testInstanceProvider().provideInstanceFor(flywayTest);
    }

    @Override
    protected List<TestRule> getTestRules(Object target) {
        List<TestRule> testRules = super.getTestRules(target);
        testRules.add(new MigrateToVersionRule(flywayTest.getMigrationVersion(), flywayTest.getDbMigrator()));
        return testRules;
    }

}
