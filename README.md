# AC API Automation (REST Assured + Cucumber)

A small BDD framework for testing the AgeChecked `AC0137` remote check API.

## Stack
- Java 11, Maven
- REST Assured for HTTP calls
- Cucumber (JUnit 4 runner) for BDD feature files
- Lombok for request POJO builders

## Configuration

`BASE_URL` and `MERCHANT_SECRET_KEY` are never hardcoded — they are read from
environment variables at runtime (see [ConfigManager](src/test/java/com/agechecked/config/ConfigManager.java)).

Set them as real OS environment variables, or copy [.env.example](.env.example) to
`.env` in the project root (git-ignored) and fill in real values:

```
BASE_URL=https://dev.agechecked.com
MERCHANT_SECRET_KEY=<your-secret-key>
```

### PowerShell (session-only)
```powershell
$env:BASE_URL = "https://dev.agechecked.com"
$env:MERCHANT_SECRET_KEY = "<your-secret-key>"
```

## Running the tests
```powershell
mvn test
```

Cucumber reports are generated at `target/cucumber-reports/cucumber.html`.

## Project layout
```
src/test/java/com/agechecked/
  config/            # environment variable / .env resolution
  models/            # request payload POJOs
  client/            # REST Assured HTTP client wrapper
  stepdefinitions/   # Cucumber step definitions
  runner/            # JUnit + Cucumber test runner
src/test/resources/features/  # .feature files
```
