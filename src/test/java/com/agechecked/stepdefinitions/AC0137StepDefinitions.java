package com.agechecked.stepdefinitions;

import com.agechecked.client.ApiClient;
import com.agechecked.config.ConfigManager;
import com.agechecked.models.AgeCheckRequest;
import com.agechecked.util.TestDataGenerator;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Allure;
import io.restassured.response.Response;

import java.util.Map;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class AC0137StepDefinitions {

    private static final String AC0137_ENDPOINT = "/api/acapiremote/ac0137";
    private static final String AUTO_PLACEHOLDER = "AUTO";

    private AgeCheckRequest request;
    private Response response;
    private String generatedReference;
    private String generatedEmail;

    @Given("the AgeChecked API base URL is configured")
    public void the_age_checked_api_base_url_is_configured() {
        assertThat("BASE_URL must be set", ConfigManager.getInstance().getBaseUrl(), notNullValue());
    }

    @Given("a valid merchant key is configured")
    public void a_valid_merchant_key_is_configured() {
        String merchantSecretKey = ConfigManager.getInstance().getMerchantSecretKey();
        assertThat("MERCHANT_SECRET_KEY must be set", merchantSecretKey, notNullValue());
        assertThat("MERCHANT_SECRET_KEY must not be blank", merchantSecretKey.isBlank(), equalTo(false));
    }

    @Given("an AC0137 request for the following applicant")
    public void an_ac0137_request_for_the_following_applicant(Map<String, String> fields) {
        generatedReference = resolveDynamicValue(fields.get("reference"), TestDataGenerator::generateReference);
        generatedEmail = resolveEmail(fields);

        request = AgeCheckRequest.builder()
                .merchantSecretKey(resolveMerchantSecretKey(fields))
                .countrycode(fields.getOrDefault("countrycode", "CA"))
                .reference(generatedReference)
                .firstname(fields.get("firstname"))
                .middlename(fields.getOrDefault("middlename", ""))
                .lastname(fields.get("lastname"))
                .dateofbirth(fields.get("dateofbirth"))
                .address1(fields.getOrDefault("address1", ""))
                .address3(fields.getOrDefault("address3", ""))
                .address4(fields.getOrDefault("address4", ""))
                .address5(fields.getOrDefault("address5", ""))
                .additionalfield1(fields.getOrDefault("additionalfield1", ""))
                .email(generatedEmail)
                .consentobtained(fields.getOrDefault("consentobtained", "YES"))
                .withforce("false")
                .build();

        Allure.addAttachment("AC0137 request payload", "application/json", request.toString());
    }

    /**
     * Returns {@code value} unchanged, unless it is the {@value #AUTO_PLACEHOLDER}
     * placeholder (or blank), in which case a fresh dynamic value is generated.
     */
    private String resolveDynamicValue(String value, Supplier<String> generator) {
        if (value == null || value.isBlank() || AUTO_PLACEHOLDER.equalsIgnoreCase(value.trim())) {
            return generator.get();
        }
        return value;
    }

    /**
     * Resolves the email field to use for the request. A fresh email is generated only when the
     * {@code email} row is absent from the applicant table altogether, or its value is the
     * {@value #AUTO_PLACEHOLDER} placeholder. A row that is present with a blank cell (which
     * Cucumber represents as a {@code null} map value) is left as {@code null}/blank so
     * scenarios can exercise the "missing mandatory email" validation.
     */
    private String resolveEmail(Map<String, String> fields) {
        String value = fields.get("email");
        if (!fields.containsKey("email") || AUTO_PLACEHOLDER.equalsIgnoreCase(value == null ? "" : value.trim())) {
            return TestDataGenerator.generateEmail();
        }
        return value;
    }

    /**
     * Resolves the merchant secret key to use for the request. The configured valid key is used
     * only when the {@code merchantkey} row is absent from the applicant table altogether, or its
     * value is the {@value #AUTO_PLACEHOLDER} placeholder. A row that is present with a blank cell
     * (which Cucumber represents as a {@code null} map value) is left as {@code null}/blank so
     * scenarios can exercise the "missing mandatory merchant key" validation.
     */
    private String resolveMerchantSecretKey(Map<String, String> fields) {
        String value = fields.get("merchantkey");
        if (!fields.containsKey("merchantkey") || AUTO_PLACEHOLDER.equalsIgnoreCase(value == null ? "" : value.trim())) {
            return ConfigManager.getInstance().getMerchantSecretKey();
        }
        return value;
    }

    @When("I send the AC0137 age verification request")
    public void i_send_the_ac0137_age_verification_request() {
        response = ApiClient.post(AC0137_ENDPOINT, request);
    }

    @Then("the response status code should be {int}")
    public void the_response_status_code_should_be(int expectedStatusCode) {
        response.then().statusCode(expectedStatusCode);
    }

    @Then("the response should contain field {string} with value {string}")
    public void the_response_should_contain_field_with_value(String field, String expectedValue) {
        assertThat(response.jsonPath().getString(field), equalTo(resolveAssertionValue(field, expectedValue)));
    }

    /**
     * Same assertion as {@link #the_response_should_contain_field_with_value}, but skipped
     * when {@code expectedValue} is blank. Used by data-driven scenarios where only some
     * Examples rows produce the field being asserted on (e.g. "error.details" is absent
     * for some error responses).
     */
    @Then("the response should contain field {string} with value {string} when present")
    public void the_response_should_contain_field_with_value_when_present(String field, String expectedValue) {
        if (expectedValue == null || expectedValue.isBlank()) {
            return;
        }
        assertThat(response.jsonPath().getString(field), equalTo(resolveAssertionValue(field, expectedValue)));
    }

    /**
     * Resolves the {@value #AUTO_PLACEHOLDER} placeholder used for the
     * {@code reference}/{@code email} fields to the value that was actually
     * generated for the request, so assertions still match the dynamic data.
     */
    private String resolveAssertionValue(String field, String expectedValue) {
        if (AUTO_PLACEHOLDER.equalsIgnoreCase(expectedValue)) {
            if ("reference".equalsIgnoreCase(field)) {
                return generatedReference;
            }
            if ("email".equalsIgnoreCase(field)) {
                return generatedEmail;
            }
        }
        return expectedValue;
    }

    @After
    public void attachResponseOnFailure(Scenario scenario) {
        if (scenario.isFailed() && response != null) {
            scenario.attach(response.getBody().asPrettyString(), "application/json", "AC0137 response");
        }
    }
}
