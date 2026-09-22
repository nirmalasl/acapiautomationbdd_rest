package com.agechecked.stepdefinitions;

import com.agechecked.client.ApiClient;
import com.agechecked.config.ConfigManager;
import com.agechecked.models.AgeCheckRequest;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.response.Response;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class AC0137StepDefinitions {

    private static final String AC0137_ENDPOINT = "/api/acapiremote/ac0137";

    private AgeCheckRequest request;
    private Response response;

    @Given("the AgeChecked API base URL is configured")
    public void the_age_checked_api_base_url_is_configured() {
        assertThat("BASE_URL must be set", ConfigManager.getInstance().getBaseUrl(), notNullValue());
    }

    @Given("an AC0137 request for the following applicant")
    public void an_ac0137_request_for_the_following_applicant(Map<String, String> fields) {
        request = AgeCheckRequest.builder()
                .merchantSecretKey(ConfigManager.getInstance().getMerchantSecretKey())
                .countrycode(fields.getOrDefault("countrycode", "CA"))
                .reference(fields.get("reference"))
                .firstname(fields.get("firstname"))
                .middlename(fields.getOrDefault("middlename", ""))
                .lastname(fields.get("lastname"))
                .dateofbirth(fields.get("dateofbirth"))
                .address1(fields.getOrDefault("address1", ""))
                .address3(fields.getOrDefault("address3", ""))
                .address4(fields.getOrDefault("address4", ""))
                .address5(fields.getOrDefault("address5", ""))
                .additionalfield1(fields.getOrDefault("additionalfield1", ""))
                .email(fields.get("email"))
                .consentobtained("YES")
                .withforce("false")
                .build();
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
        assertThat(response.jsonPath().getString(field), equalTo(expectedValue));
    }

        @Then("the service response {string} should include scores")
        public void the_service_response_should_include_scores(String serviceName) {
        System.out.println("DEBUG AC0137 response body: " + response.getBody().asPrettyString());
        List<Map<String, Object>> serviceResponses = response.jsonPath().getList("datacontent.serviceResponses");
        assertThat("serviceResponses must be present", serviceResponses, notNullValue());

        Map<String, Object> serviceResponse = serviceResponses.stream()
                .filter(item -> serviceName.equals(item.get("serviceName")))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing service response: " + serviceName));

        assertThat("safeHarbourScore must be present for " + serviceName,
            serviceResponse.get("safeHarbourScore"), notNullValue());
        assertThat("nameMatchScore must be present for " + serviceName,
            serviceResponse.get("nameMatchScore"), notNullValue());
        assertThat("addressMatchScore must be present for " + serviceName,
            serviceResponse.get("addressMatchScore"), notNullValue());
    }

    @After
    public void attachResponseOnFailure(Scenario scenario) {
        if (scenario.isFailed() && response != null) {
            scenario.attach(response.getBody().asPrettyString(), "application/json", "AC0137 response");
        }
    }
}
