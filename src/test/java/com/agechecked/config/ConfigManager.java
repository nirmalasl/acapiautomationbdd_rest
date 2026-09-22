package com.agechecked.config;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Resolves configuration from OS environment variables, falling back to a
 * local .env file (useful for local development). Required values:
 * BASE_URL, MERCHANT_SECRET_KEY.
 */
public final class ConfigManager {

    private static final Dotenv DOTENV = Dotenv.configure().ignoreIfMissing().load();
    private static final ConfigManager INSTANCE = new ConfigManager();

    private final String baseUrl;
    private final String merchantSecretKey;

    private ConfigManager() {
        this.baseUrl = requireValue("BASE_URL");
        this.merchantSecretKey = requireValue("MERCHANT_SECRET_KEY");
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

    private static String requireValue(String key) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = DOTENV.get(key);
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required environment variable: " + key
                    + ". Set it as an OS environment variable or define it in a .env file at the project root.");
        }
        return value;
    }
}
