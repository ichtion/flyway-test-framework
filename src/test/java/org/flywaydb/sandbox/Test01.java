package org.flywaydb.sandbox;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class Test01 {
    @BeforeClass
    public static void beforeClass() {
        System.out.println("Before class Test01");
    }

    @Test
    public void test() {
        System.out.println("Test01");
    }

    @AfterClass
    public static void afterClass() {
        System.out.println("After class Test01");
    }
}
