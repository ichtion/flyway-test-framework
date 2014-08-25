package org.flywaydb.util;

import java.util.UUID;

public class TestUtils {

    public static String id() {
        return UUID.randomUUID().toString();
    }
}
