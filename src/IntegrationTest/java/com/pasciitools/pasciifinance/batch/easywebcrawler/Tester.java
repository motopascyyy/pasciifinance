package com.pasciitools.pasciifinance.batch.easywebcrawler;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;


public class Tester {
    private final Logger log = LoggerFactory.getLogger(Tester.class);

    private WebDriver driver;
    private WebDriverWait wait;

    @Value(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY)
    private String chromeDriverLocation;

    @Test
    public void testLogin () {
        try {
            driver = getDriver();
            loginDriver();
        } catch (MalformedURLException e) {
            Assertions.fail();
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail();
        }
        finally {
            driver.quit();
            driver.close();
        }

    }

    public WebDriver getDriver () throws MalformedURLException {
        var options = new ChromeOptions();
        try {
//            ChromeDriverService service = new ChromeDriverService (new File("/Users/pasdeignan/chromedriver"), 9515, Duration.ofSeconds(20),new ArrayList<String>(), new HashMap<String, String>());
//            return new ChromeDriver(service, options);
            if (chromeDriverLocation != null) {
                System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, chromeDriverLocation);
            }
            return new ChromeDriver(options);
        } catch (Exception e) {
            return new RemoteWebDriver(
                    new URL("http://127.0.0.1:9515"),
                    options);
        }
    }

    private void loginDriver () {
        driver.get("https://easyweb.td.com/");
        var usernameField = getUserNameField();
        WebElement passwordField = driver.findElement(By.id("uapPassword"));
        usernameField.sendKeys("4724090087160022");
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            //Just a dummy catch.
        }
        passwordField.sendKeys("6PvoEIH4ZT7d");
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            //Just a dummy catch.
        }
        click(By.cssSelector(".login-form button.td-button-secondary"));
//        passwordField.sendKeys(Keys.ENTER);
        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            //Just a dummy catch.
        }
    }

    private void click(By by) {
        try {
            WebElement button = driver.findElement(by);
            click(button);
        } catch (NoSuchElementException e){
            log.error(String.format("Could not find the element: %s", by.toString()), e);
        }
    }

    private WebElement getUserNameField () {
        WebElement field = null;

        var fieldOptionsStrings = "username,username101".split(",");
        for (String option : fieldOptionsStrings) {
            var by = By.id(option);
            if (!driver.findElements(by).isEmpty()) {
                field = driver.findElement(by);
                String message = String.format("Using field '%s' of tag name <%s /> to inject data", option, field.getTagName());
                log.info(message);
                break;
            }
        }
        if (field == null)
            log.warn("Could not find any fields matching the css ids provided. This probably isn't going to end well.");
        return field;
    }

    private void click (WebElement we) {
        try {
            we.click();
        } catch (NoSuchElementException e){
            log.error(String.format("Could not find the element: %s.%nNested Exception: %s", we.toString(), e.getMessage()), e);

        }
    }

}
