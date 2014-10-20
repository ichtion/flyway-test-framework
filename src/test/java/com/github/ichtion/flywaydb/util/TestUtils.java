package com.github.ichtion.flywaydb.util;

import java.util.Random;
import java.util.UUID;

public class TestUtils {

    public static String id() {
        return UUID.randomUUID().toString();
    }

    public static void sleepRandomTime() {
        try {
            Thread.sleep(new Long(new Random().nextInt(100)*50).longValue());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
