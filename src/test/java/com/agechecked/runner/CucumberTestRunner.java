package com.agechecked.runner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = "com.agechecked.stepdefinitions",
        plugin = {"pretty", "summary", "html:target/cucumber-reports/cucumber.html"},
        monochrome = true
)
public class CucumberTestRunner {
}
