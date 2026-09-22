package com.agechecked.client;

import com.agechecked.config.ConfigManager;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

/**
 * Thin wrapper around REST Assured for calling the AgeChecked API.
 * Every call is attached to the Allure report via the AllureRestAssured filter.
 */
public final class ApiClient {

    private ApiClient() {
    }

    @Step("POST {endpoint}")
    public static Response post(String endpoint, Object body) {
        return given()
                .filter(new AllureRestAssured())
                .baseUri(ConfigManager.getInstance().getBaseUrl())
                .contentType("application/json")
                .body(body)
                .when()
                .post(endpoint);
    }
}
