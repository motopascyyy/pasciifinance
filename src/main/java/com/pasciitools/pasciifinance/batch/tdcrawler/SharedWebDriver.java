package com.pasciitools.pasciifinance.batch.tdcrawler;

import com.pasciitools.pasciifinance.batch.Site;
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

import java.io.File;
import java.time.Duration;


/*
* WARNING: This code is not thread safe. If a thread calls the shared instance and is executing a job, and another thread tries to quit, it will impact the original thread.
* */
public class SharedWebDriver {
    private static SharedWebDriver sharedDriver = null;
    private static final String TWO_FA_DIALOG_ID = "mat-dialog-0";
    private static final String CARET = "td-wb-dropdown-toggle__caret";
    private final Logger log = LoggerFactory.getLogger(SharedWebDriver.class);

    @Value("${usernameFieldOptions}")
    private String usernameFieldOptions;

    @Value("${easyweb.userName}")
    private String ewUserName;
    @Value("${easyweb.password}")
    private String ewPassword;
    @Value("${easyweb.url}")
    private String easyWebUrl;

    @Value("${webbroker.userName}")
    private String wbUserName;
    @Value("${webbroker.password}")
    private String wbPassword;
    @Value("${webbroker.url}")
    private String webBrokerUrl;
    private WebDriverWait wait;

    public WebDriver getDriver() {
        if (driver == null) {
            initializeWebDriver();
        }
        return driver;
    }

    private WebDriver driver;

    @Value("${webdriver.chrome.driver}")
    private String chromeDriverLocation;

    private SharedWebDriver () {
        initializeWebDriver();
    }

    public static SharedWebDriver getInstance () {
        if (sharedDriver == null) {
            sharedDriver = new SharedWebDriver();
        } else if (sharedDriver.getDriver() == null) {
            sharedDriver.initializeWebDriver();
        }
        return sharedDriver;
    }

    private void initializeWebDriver () {
        var options = new ChromeOptions();

        // Check to see if an alternate chromedriver binary was specific
        if (StringUtils.isNotEmpty(chromeDriverLocation)) {
            File f = new File(chromeDriverLocation);
            if (f.exists())
                System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY, chromeDriverLocation);
        }
        driver = new ChromeDriver(options);
    }

    public void killDriver () {
        if (driver != null) {
            driver.quit();
            driver = null;
            log.debug("Shared Web Driver has been killed.");
        } else
            log.debug("Call to quit the Shared Web Driver but either the Singleton instance of SharedWebDriver or the underlying WebDriver were null. Nothing happened.");
    }

    public boolean loginDriver (Site siteEnum, String userName, String password) {

        String url;
        ExpectedCondition<Boolean> successfulCondition;
        switch (siteEnum) {
            case TD_EASYWEB:
                url = easyWebUrl;
                successfulCondition = ExpectedConditions.urlToBe("https://easyweb.td.com/waw/ezw/webbanking");
                break;
            case TD_WEBBROKER:
                url = webBrokerUrl;
                successfulCondition = ExpectedConditions.urlContains("https://webbroker.td.com/waw/brk/wb/wbr/static/main/index.html");
                break;
            default:
                url = easyWebUrl;
                successfulCondition = ExpectedConditions.urlToBe("https://easyweb.td.com/waw/ezw/webbanking");
                break;
        }
        driver = getDriver(); //this is to ensure the driver inside the shared instance hasn't been set to null or anything.
        driver.get(url);
        var usernameField = getUserNameField();
        WebElement passwordField = driver.findElement(By.id("uapPassword"));
        usernameField.sendKeys(userName);
        passwordField.sendKeys(password);
        click(By.cssSelector(".form-group > button"));
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        waitFor2FA();
        return wait.until(successfulCondition);
    }

    protected void waitFor2FA () {

        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.id(TWO_FA_DIALOG_ID)));
            log.debug("Found 2FA dialog. User expected to enter to manually handle the 2FA code entry since TD still uses SMS like luddites.");
            Thread.sleep(30000l);
            var twoFAWait = new WebDriverWait(driver, Duration.ofSeconds(120), Duration.ofMillis(1000));
            twoFAWait.until((ExpectedCondition<Boolean>) d -> (d.findElements(By.id(TWO_FA_DIALOG_ID)).isEmpty()));
        } catch(NoSuchElementException | TimeoutException e) {
            log.debug("Did not find 2FA dialog. Exception means we don't need to deal with 2FA and cna proceed normally.");
        } catch (InterruptedException e) {
            log.error("Thread was interrupted while performing sleep function waiting for the 2FA code. This will cause problems with the rest of the execution.");
        }
    }

    public WebElement getUserNameField () {
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
