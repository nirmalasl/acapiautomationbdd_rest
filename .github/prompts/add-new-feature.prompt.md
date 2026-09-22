---
agent: agent
description: 'Add a new Cucumber feature file and matching step definitions for a new AgeChecked API endpoint without breaking existing features/tests.'
---

# Add a New Feature File + Step Definitions

You are working in the `acapiautomationbdd_rest_new` BDD REST automation
framework (Java 17 + Maven + REST Assured + Cucumber/JUnit 4 + Allure). Your
job is to add a **new** feature (e.g. a new AgeChecked endpoint such as
`AC0138`) end-to-end, following the existing conventions exactly, while
leaving every existing feature file, step definition, and shared class
working unchanged.

Treat this as **additive-only** work unless the user explicitly asks you to
modify shared/existing code.

## 0. Gather context first

Before writing anything, inspect the existing implementation for the closest
analogous endpoint and reuse its patterns:

- [ac0137_age_verification.feature](../../src/test/resources/features/ac0137_age_verification.feature) — feature file structure (Background, Scenario Outline, Examples tables).
- [AC0137StepDefinitions.java](../../src/test/java/com/agechecked/stepdefinitions/AC0137StepDefinitions.java) — step definition conventions.
- [AgeCheckRequest.java](../../src/test/java/com/agechecked/models/AgeCheckRequest.java) — request POJO (Lombok `@Data @Builder`).
- [ApiClient.java](../../src/test/java/com/agechecked/client/ApiClient.java) — shared REST Assured wrapper.
- [ConfigManager.java](../../src/test/java/com/agechecked/config/ConfigManager.java) — env/config resolution (`BASE_URL`, `MERCHANT_SECRET_KEY`).
- [TestDataGenerator.java](../../src/test/java/com/agechecked/util/TestDataGenerator.java) — dynamic/unique test data helpers.
- [CucumberTestRunner.java](../../src/test/java/com/agechecked/runner/CucumberTestRunner.java) — glue/feature discovery config.

Ask the user for anything you cannot infer from the codebase or their
request: the new endpoint path, its request/response field contract, and any
sample success/error payloads.

## 1. Create the feature file (additive)

- Add a **new** file under [features/](../../src/test/resources/features), named
  `<endpointid>_<short_description>.feature` (lowercase, snake_case), e.g.
  `ac0138_<something>.feature`. Do not edit existing `.feature` files.
- Mirror the existing `Feature:` / `Background:` structure:
  - `Background` should reuse the existing generic steps
    (`the AgeChecked API base URL is configured`, `a valid merchant key is
    configured`) — do not duplicate config-assertion logic in a new step.
- Prefer `Scenario Outline` + `Examples` for data-driven cases (missing
  mandatory fields, invalid values, happy path), consistent with
  `ac0137_age_verification.feature`.
- Use the `AUTO` placeholder convention for fields that must be dynamically
  generated (e.g. `reference`, `email`) or resolved from config
  (`merchantkey`), matching the `resolveDynamicValue` / `AUTO_PLACEHOLDER`
  pattern in `AC0137StepDefinitions`. Note that `resolveDynamicValue` and
  `AUTO_PLACEHOLDER` are `private` to that class, so a new step definitions
  class must replicate the pattern locally (its own private constant/helper)
  rather than calling into `AC0137StepDefinitions` directly.
- Word steps so they read naturally as Given/When/Then and can be
  implemented as **new** step definition methods — do not silently reuse an
  existing step's wording for a different meaning.

## 2. Reuse vs. add step definitions

- If a step is truly generic and endpoint-agnostic (e.g. "the response status
  code should be {int}", "the response should contain field {string} with
  value {string}"), reuse the existing steps in
  [AC0137StepDefinitions.java](../../src/test/java/com/agechecked/stepdefinitions/AC0137StepDefinitions.java)
  rather than duplicating them — Cucumber glue is shared across all feature
  files. **Never** change the wording/regex or behavior of an existing
  `@Given/@When/@Then` method; that will silently break every feature file
  already using it. If a shared step needs new behavior, add a new step
  instead.
- For endpoint-specific behavior (building the request payload, calling the
  endpoint), create a **new** step definitions class,
  `<Endpoint>StepDefinitions.java` (e.g. `AC0138StepDefinitions.java`), in
  [stepdefinitions/](../../src/test/java/com/agechecked/stepdefinitions),
  following `AC0137StepDefinitions`'s shape:
  - A private endpoint constant (`private static final String ACxxxx_ENDPOINT = "/api/acapiremote/acxxxx";`).
  - A `@Given` step that builds the request model from the data table,
    generating dynamic values via `TestDataGenerator` and resolving config
    via `ConfigManager`, attaching the payload to Allure.
  - A `@When` step that calls `ApiClient.post(...)` and stores the `Response`.
  - Only add new `@Then` steps if existing generic ones
    (status code / field-value assertions) don't cover the new response
    shape.
  - An `@After` hook to attach the response body to Allure on failure, scoped
    to this class's own `response` field (do not touch other classes' state).
- Do not rename or move existing classes/packages.

## 3. Request/response models

- If the new endpoint has a different payload shape, add a **new** Lombok
  `@Data @Builder` POJO under
  [models/](../../src/test/java/com/agechecked/models), named after the
  endpoint (e.g. `AgeCheckRequestAC0138.java` or a more descriptive name) —
  do not add unrelated fields to `AgeCheckRequest` "just in case", and do not
  remove/rename its existing fields (they're relied on by AC0137 tests and
  serialize 1:1 to the API's JSON contract).
- If the new endpoint's payload is a strict superset/subset of an existing
  model and reuse is genuinely simpler, confirm with the user before sharing
  a model across endpoints.

## 4. Wiring — verify, don't duplicate

- [CucumberTestRunner.java](../../src/test/java/com/agechecked/runner/CucumberTestRunner.java)
  already points `features` at the whole `src/test/resources/features`
  directory and `glue` at the whole `com.agechecked.stepdefinitions` /
  `com.agechecked.hooks` packages — a new feature file or step definitions
  class in the right folder is picked up automatically. Do not add
  per-feature runners or duplicate `@CucumberOptions` unless the user
  explicitly asks for a separate tagged run.
- If the new scenarios need a new `@Tag`, add it in the feature file only;
  don't change existing tags on other features.

## 5. Validate before finishing

1. Compile: `mvn -q test-compile` — must be clean with no errors/warnings
   about duplicate/ambiguous step definitions.
2. Run the full suite (not just the new file) to confirm nothing else
   broke: `mvn test`. If `.env` isn't configured in this environment, at
   minimum run `mvn -q test-compile` and explain that full execution needs a
   valid `.env` (see [README.md](../../README.md) Configuration section).
3. Run just the new feature if you can scope it (e.g. via `@Tag` + `mvn test
   -Dcucumber.filter.tags="@yourtag"`), and confirm the previously-existing
   AC0137 scenarios still pass unchanged.
4. Check for "Undefined step" / "Ambiguous step definitions" in the Cucumber
   output — both indicate a wiring mistake to fix before finishing.

## 6. Guardrails — do not

- Do not edit `ac0137_age_verification.feature` or
  `AC0137StepDefinitions.java` to fit the new feature's needs.
- Do not change `ConfigManager`, `ApiClient`, `Hooks`, or
  `CucumberTestRunner` unless the new feature genuinely requires a shared
  capability (e.g. a new HTTP verb) — and if so, extend them additively
  (new method/overload) rather than altering existing signatures/behavior.
- Do not change existing field names on `AgeCheckRequest` — they map
  directly to the API's JSON contract.
- Do not hardcode `BASE_URL` / `MERCHANT_SECRET_KEY` or any secret values in
  the new feature/step files — always resolve via `ConfigManager`.
- Do not introduce a second Cucumber runner or alter `pom.xml` test
  configuration unless explicitly requested.
