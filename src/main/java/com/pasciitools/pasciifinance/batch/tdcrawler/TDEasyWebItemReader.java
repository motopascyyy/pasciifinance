package com.pasciitools.pasciifinance.batch.tdcrawler;

import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.service.AccountService;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class TDEasyWebItemReader extends TDItemReader implements ItemReader<AccountEntry> {


    private final AccountService accountService;
    private final NumberFormat nfCAD = NumberFormat.getCurrencyInstance(Locale.CANADA);
    private final Logger log = LoggerFactory.getLogger(TDEasyWebItemReader.class);

    private final String webBrokerURL;

    private final String userName;
    private final String password;

    private List<AccountEntry> entries;
    private Iterator<AccountEntry> iter;

    @Value("${usernameFieldOptions}")
    private String usernameFieldOptions;

    @Value("${webdriver.chrome.driver}")
    private String chromeDriverLocation;


    @Override
    public AccountEntry read() throws UnexpectedInputException, ParseException, MalformedURLException, InterruptedException {

        if (entries == null) {
            log.debug("Collecting all the data from WebBroker. This part will take a while. Subsequent steps will be much faster.");
            String killMessage = "Killing job.";
            try {
                driver = getDriver();
                loginDriver(webBrokerURL, userName, password);
                wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                waitFor2FA();
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
                        killMessage;
                logAndKillDriver(e, message);
                throw new MalformedURLException(message);
            } catch (InterruptedException e) {
                String message = "Thread interrupted during execution. " +
                        killMessage;
                logAndKillDriver(e, message);
                Thread.currentThread().interrupt();
                throw new InterruptedException(message);
            } catch (TimeoutException e) {
                String message = "Timeout Exception during execution. " +
                        killMessage;
                logAndKillDriver(e, message);
                throw new TimeoutException(message, e);
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
        List<AccountEntry> aEntries= new ArrayList<>();
        var currentDate = LocalDateTime.now();

        //starting at i == 1 because the first row is a header row
        //skipping last 2 rows as well as they're either a summary row or the row to open a new account
        for (var i = 1; i < bankingRows.size() - 2; i++) {
            WebElement row = bankingRows.get(i);
            WebElement accountSpan = row.findElement(By.tagName("span"));
            WebElement accountIdSpan = accountSpan.findElement(By.tagName("span")); //yes, for some reason the span is nested within another span...
            var accountEntry = new AccountEntry();
            String accountNumber = accountIdSpan.getText().replace("›","").trim();
            String cleanAccountNumber = cleanTextContent(accountNumber);
            var acc = accountService.getAccountFromAccountNumber(cleanAccountNumber);
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
            aEntries.add(accountEntry);
        }
        log.info("Finished parsing for all Web Broker details");
        return aEntries;
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

    private double getValue (WebElement el, By by) throws NoSuchElementException{
        double result = 0;
        WebElement totalValueDiv = null;
        try {
            totalValueDiv = el.findElement(by);
            result = nfCAD.parse(totalValueDiv.getText()).doubleValue();
        } catch (java.text.ParseException | TimeoutException e) {
            String parsedValue = totalValueDiv != null ? totalValueDiv.getText() : "null";
            var message = String.format("Failed to get value of element %s. " +
                    "Value found for `By` was %s. " +
                    "Failed to retrieve because of %s. Returning NaN.",
                    by.toString(), parsedValue, e.getMessage());
            log.warn(message, e);
            result = Double.NaN;
        } catch (NoSuchElementException e) {
            var message = String.format("Could not find element %s.", by.toString());
            throw new NoSuchElementException(message, e);
        }
        return result;
    }
}
