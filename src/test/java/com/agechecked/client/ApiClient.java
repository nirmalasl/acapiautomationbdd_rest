package com.agechecked.client;

import com.agechecked.config.ConfigManager;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * Thin wrapper around REST Assured for calling the AgeChecked API.
 */
public final class ApiClient {

    private ApiClient() {
    }

    public static Response post(String endpoint, Object body) {
        return given()
                .baseUri(ConfigManager.getInstance().getBaseUrl())
                .contentType("application/json")
                .body(body)
                .when()
                .post(endpoint);
    }
}
