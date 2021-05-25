package com.pasciitools.pasciifinance.batch.easywebcrawler;

import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.service.AccountService;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class TDEasyWebItemReader implements ItemReader<AccountEntry> {


    private AccountService accountService;
    private final NumberFormat nfCAD = NumberFormat.getCurrencyInstance(Locale.CANADA);
    private final NumberFormat pctFormatter = NumberFormat.getPercentInstance(Locale.CANADA);
    private final DecimalFormat decPctFormatter = (DecimalFormat) DecimalFormat.getPercentInstance(Locale.CANADA);
    private WebDriver driver;
    private WebDriverWait wait;
    private Logger log = LoggerFactory.getLogger(TDEasyWebItemReader.class);

    private String webBrokerURL;

    private String userName;
    private String password;

    private List<AccountEntry> entries;
    private Iterator<AccountEntry> iter;

    @Override
    public AccountEntry read() throws UnexpectedInputException, ParseException, NonTransientResourceException {

        if (entries == null) {
            log.debug("Collecting all the data from WebBroker. This part will take a while. Subsequent steps will be much faster.");
            try {
                driver = getDriver();
                loginDriver();
                wait = new WebDriverWait(driver, 10);
                boolean redirectCompleted = wait.until(ExpectedConditions.urlToBe("https://easyweb.td.com/waw/ezw/webbanking"));
                if (redirectCompleted){
                    driver.switchTo().frame("tddetails");
                    WebElement tdContentAreaDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("td-layout-contentarea")));
                    log.debug("Login successful. Proceeding to data collection.");
                    WebElement bankingDiv = tdContentAreaDiv.findElement(By.className("td-target-banking"));
                    List<WebElement> rows = bankingDiv.findElements(By.tagName("tr"));
                    entries = collectData(rows);
                    iter = entries.iterator();
                    if (driver != null) {
                        driver.quit();
                        driver = null;
                        log.debug("Quitting browser since no longer necessary to read entries. Everything collected from Web Broker.");
                    }
                } else {
                    throw new TimeoutException("EasyWeb redirect did not complete in time. Exiting");
                }
            } catch (MalformedURLException e) {
                String message = "URL was malformed. Could not establish connection. " +
                        "Throwing RuntimeException to kill the process.";
                log.error(message, e);
                if (driver != null) {
                    driver.quit();
                    driver = null;
                }
                throw new NonTransientResourceException(message, e);
            } catch (InterruptedException e) {
                String message = "Thread interrupted during execution. " +
                        "Throwing RuntimeException to kill the process.";
                log.error(message, e);
                if (driver != null) {
                    driver.quit();
                    driver = null;
                }
                throw new NonTransientResourceException(message, e);
            } catch (TimeoutException e) {
                String message = "Timeout Exception during execution. " +
                        "Throwing RuntimeException to kill the process.";
                log.error(message, e);
                if (driver != null) {
                    driver.quit();
                    driver = null;
                }
                throw new NonTransientResourceException(message, e);
            }
        }

        AccountEntry nextEntry;
        if (iter != null && iter.hasNext()) {
            nextEntry = iter.next();
        } else {
            nextEntry = null;
            entries = null;
        }
        return nextEntry;
    }

    public TDEasyWebItemReader(String userName, String password, AccountService accountService, String url) {

        this.userName = userName;
        this.password = password;
        this.accountService = accountService;
        this.webBrokerURL = url;
        if (userName == null || password == null)
            throw new RuntimeException("Credentials not provided. Crashing execution");

    }

    private List<AccountEntry> collectData (List<WebElement> bankingRows) throws InterruptedException {
        List<AccountEntry> entries= new ArrayList<>();
        var zero = new BigDecimal(0);
        Date currentDate = new Date();

        //starting at i == 1 because the first row is a header row
        //skipping last 2 rows as well as they're either a summary row or the row to open a new account
        for (int i = 1; i < bankingRows.size() - 2; i++) {
            WebElement row = bankingRows.get(i);
            WebElement accountSpan = row.findElement(By.tagName("span"));
            log.info(String.format("accountSpan text: %s", accountSpan.getText()));
            WebElement accountIdSpan = accountSpan.findElement(By.tagName("span")); //yes, for some reason the span is nested within another span...
            log.info(String.format("accountIdSpan text: %s", accountIdSpan.getText()));
            AccountEntry accountEntry = new AccountEntry();
            String accountNumber = accountIdSpan.getText().replace("›","").trim();
            String cleanAccountNumber = cleanTextContent(accountNumber);
            Account acc = accountService.getAccountFromAccountNumber(cleanAccountNumber);
            if (acc != null)
                accountEntry.setAccount(acc);
            else
                throw new InterruptedException(String.format("Account not found for account number: %s", accountNumber));

            double amount = 0;
            try {
                amount = getValue(row, By.className("td-copy-align-right"));
            } catch (NoSuchElementException e) {
                log.error(String.format("Could not capture amount. Setting value to $0. Tried searching from this row: %s",  row), e);
            }
            if (amount != Double.NaN) {
                accountEntry.setMarketValue(amount);
                accountEntry.setBookValue(amount);
                assignAllocations(accountEntry);
            } else {
                accountEntry.setBookValue(0);
                accountEntry.setMarketValue(0);
            }
            accountEntry.setEntryDate(currentDate);
            entries.add(accountEntry);
        }
        log.info("Finished parsing for all Web Broker details");
        return entries;
    }

    private String cleanTextContent(String text) {
        // strips off all non-ASCII characters
        text = text.replaceAll("[^\\x00-\\x7F]", "");

        // erases all the ASCII control characters
        text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

        // removes non-printable characters from Unicode
        text = text.replaceAll("\\p{C}", "");

        return text.trim();
    }

    private void assignAllocations (AccountEntry accountEntry) {
        accountEntry.setCanadianEqtPct(BigDecimal.ZERO);
        accountEntry.setUsEqtPct(BigDecimal.ZERO);
        accountEntry.setInternationalEqtPct(BigDecimal.ZERO);
        accountEntry.setCadFixedIncomePct(BigDecimal.ZERO);
        accountEntry.setGlobalFixedIncomePct(BigDecimal.ZERO);
        accountEntry.setCashPct(BigDecimal.ONE);
        accountEntry.setOtherPct(BigDecimal.ZERO);
    }

    private void loginDriver () {
        driver.get(webBrokerURL);
        WebElement usernameField = driver.findElement(By.id("username100"));
        WebElement passwordField = driver.findElement(By.id("password"));
        usernameField.sendKeys(userName);
        passwordField.sendKeys(password);
        click(By.cssSelector(".form-group > button"));
    }

    public WebDriver getDriver () throws MalformedURLException {
        ChromeOptions options = new ChromeOptions();

        return new RemoteWebDriver(
                new URL("http://127.0.0.1:9515"),
                options);
    }

    private double getValue (WebElement el, By by) throws NoSuchElementException{
        double result = 0;
        WebElement totalValueDiv = null;
        try {
            totalValueDiv = el.findElement(by);
            result = nfCAD.parse(totalValueDiv.getText()).doubleValue();
        } catch (java.text.ParseException | TimeoutException e) {
            String parsedValue = totalValueDiv != null ? totalValueDiv.getText() : "null";
            String message = String.format("Failed to get value of element %s. " +
                    "Value found for `By` was %s. " +
                    "Failed to retrieve because of %s. Returning NaN.",
                    by.toString(), parsedValue, e.getMessage());
            log.warn(message, e);
            result = Double.NaN;
        } catch (NoSuchElementException e) {
            String message = String.format("Could not find element %s.", by.toString());
            throw new NoSuchElementException(message, e);
        }
        return result;
    }


    private void click(By by) {
        try {
            WebElement button = driver.findElement(by);
            click(button);
        } catch (NoSuchElementException e){
            log.error(String.format("Could not find the element: %s", by.toString()), e);
        }
    }

    private void click (WebElement we) {
        try {
            we.click();
        } catch (NoSuchElementException e){
            log.error(String.format("Could not find the element: %s.\nNested Exception: %s", we.toString(), e.getMessage()), e);

        }
    }
}
