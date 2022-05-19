package com.pasciitools.pasciifinance.batch.tdcrawler;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.Locale;

public class TDItemReader {

    private final Logger log = LoggerFactory.getLogger(TDItemReader.class);
    @Value("${usernameFieldOptions}")
    private String usernameFieldOptions;

    @Value("${webdriver.chrome.driver}")
    private String chromeDriverLocation;
    private static final String TWO_FA_DIALOG_ID = "mat-dialog-0";
    private final NumberFormat nfCAD = NumberFormat.getCurrencyInstance(Locale.CANADA);
    protected WebDriver driver;
    protected WebDriverWait wait;

    protected WebDriver getDriver () throws MalformedURLException {
        var options = new ChromeOptions();

        // Check to see if an alternate chromedriver binary was specific
        if (StringUtils.isNotEmpty(chromeDriverLocation))
            System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, chromeDriverLocation);
        return new ChromeDriver(options);
    }

    protected void logAndKillDriver (Exception e, String message) {
        log.error(message, e);
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    protected void waitFor2FA () {

        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.id(TWO_FA_DIALOG_ID)));
            log.debug("Found 2FA dialog. User expected to enter to manually handle the 2FA code entry since TD still uses SMS like luddites.");
            var twoFAWait = new WebDriverWait(driver, Duration.ofSeconds(120), Duration.ofMillis(1000));
            twoFAWait.until((ExpectedCondition<Boolean>) d -> (d.findElements(By.id(TWO_FA_DIALOG_ID)).isEmpty()));
        } catch(NoSuchElementException | TimeoutException e) {
            log.debug("Did not find 2FA dialog. Exception means we don't need to deal with 2FA and cna proceed normally.");
        }
    }

    protected void loginDriver (String url, String userName, String password) {
        driver.get(url);
        var usernameField = getUserNameField();
        WebElement passwordField = driver.findElement(By.id("uapPassword"));
        usernameField.sendKeys(userName);
        passwordField.sendKeys(password);
        click(By.cssSelector(".form-group > button"));
    }

    private WebElement getUserNameField () {
        WebElement field = null;

        var fieldOptionsStrings = usernameFieldOptions.split(",");
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

    protected void click(By by) {
        try {
            WebElement button = driver.findElement(by);
            click(button);
        } catch (NoSuchElementException e){
            log.error(String.format("Could not find the element: %s", by.toString()), e);
        }
    }

    protected void click (WebElement we) {
        try {
            we.click();
        } catch (NoSuchElementException e){
            log.error(String.format("Could not find the element: %s.%nNested Exception: %s", we.toString(), e.getMessage()), e);

        }
    }
}
