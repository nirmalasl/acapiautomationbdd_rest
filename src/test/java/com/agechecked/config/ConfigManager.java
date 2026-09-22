package com.agechecked.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

/**
 * Resolves configuration values so the same framework/feature files can run
 * unchanged regardless of which target the {@code .env} file (or OS
 * environment variables) points at.
 *
 * <p>The {@code .env} file holds BASE_URL / MERCHANT_SECRET_KEY values for
 * both {@code dev} and {@code staging}, prefixed accordingly
 * (e.g. {@code DEV_BASE_URL}, {@code STAGING_MERCHANT_SECRET_KEY}). The
 * {@code ENV} variable selects which pair is active.
 *
 * <p>Value resolution for ENV / BASE_URL / MERCHANT_SECRET_KEY (highest priority first):
 * <ol>
 *   <li>OS environment variable (e.g. set by CI) - always wins</li>
 *   <li>Java system property, e.g. {@code -Denv=staging} on the Maven command line
 *       (checked both as-is and lower-cased, so the {@code env} Maven property maps to {@code ENV})</li>
 *   <li>The single {@code .env} file at the project root (git-ignored)</li>
 * </ol>
 */
public final class ConfigManager {

    private static final ConfigManager INSTANCE = new ConfigManager();

    private final Dotenv dotenv;

    private final String baseUrl;
    private final String merchantSecretKey;

    private ConfigManager() {
        this.dotenv = loadDotenv(".env");

        String env = resolveValue("ENV", "dev").trim().toLowerCase();
        if (!env.equals("dev") && !env.equals("staging")) {
            throw new IllegalStateException("Invalid ENV value: " + env + ". Expected 'dev' or 'staging'.");
        }
        String prefix = env.toUpperCase() + "_";

        this.baseUrl = requireValue(prefix + "BASE_URL");
        this.merchantSecretKey = requireValue(prefix + "MERCHANT_SECRET_KEY");
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getMerchantSecretKey() {
        return merchantSecretKey;
    }

    private static Dotenv loadDotenv(String filename) {
        try {
            return Dotenv.configure().filename(filename).ignoreIfMissing().load();
        } catch (DotenvException e) {
            throw new IllegalStateException("Failed to read " + filename, e);
        }
    }

    private String requireValue(String key) {
        String value = resolveValue(key, null);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required configuration value: " + key
                    + ". Set it as an OS environment variable, or define it in the .env file at the project root.");
        }
        return value;
    }

    private String resolveValue(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            // Supports e.g. "mvn test -Denv=staging" (Maven's <env> property is passed
            // through as the lower-cased system property "env", not "ENV").
            value = System.getProperty(key);
        }
        if (value == null || value.isBlank()) {
            value = System.getProperty(key.toLowerCase());
        }
        if (value == null || value.isBlank()) {
            value = dotenv.get(key);
        }
        if (value == null || value.isBlank()) {
            value = defaultValue;
        }
        return value;
    }
}
