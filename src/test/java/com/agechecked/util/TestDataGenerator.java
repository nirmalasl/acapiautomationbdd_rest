package com.agechecked.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generates unique, dynamic test data values (currently {@code reference} and
 * {@code email}) so scenarios don't collide with data left over from
 * previous runs against a shared environment.
 *
 * <p>All generated values are stamped with the {@value #PREFIX} prefix,
 * followed by the current time in milliseconds and a per-JVM counter to
 * guarantee uniqueness even when multiple values are generated within the
 * same millisecond.
 */
public final class TestDataGenerator {

    private static final String PREFIX = "ac-auto";
    private static final String EMAIL_DOMAIN = "agechecked.com";
    private static final AtomicInteger COUNTER = new AtomicInteger();

    private TestDataGenerator() {
    }

    public static String generateReference() {
        return PREFIX + "-" + uniqueSuffix();
    }

    public static String generateEmail() {
        return PREFIX + "-" + uniqueSuffix() + "@" + EMAIL_DOMAIN;
    }

    private static String uniqueSuffix() {
        return System.currentTimeMillis() + "-" + COUNTER.incrementAndGet();
    }
}
