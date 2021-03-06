package com.pasciitools.pasciifinance.batch.tdcrawler;

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
import java.time.LocalDateTime;
import java.util.*;

public class TDWebBrokerItemReader implements ItemReader<AccountEntry> {


    private AccountService accountService;
    private final NumberFormat nfCAD = NumberFormat.getCurrencyInstance(Locale.CANADA);
    private final NumberFormat pctFormatter = NumberFormat.getPercentInstance(Locale.CANADA);
    private final DecimalFormat decPctFormatter = (DecimalFormat) DecimalFormat.getPercentInstance(Locale.CANADA);
    private WebDriver driver;
    private WebDriverWait wait;
    private Logger log = LoggerFactory.getLogger(TDWebBrokerItemReader.class);

    private String webBrokerURL;

    private String userName;
    private String password;

    private final String CARRET = "td-wb-dropdown-toggle__caret";
    private final String ACTIVE_CARRET = "td-wb-dropdown-toggle--active";
    private final String PARENT_XPATH = "./..";
    private final String CHILDREN_XPATH_ONE_DOWN = "./*";
    private final String CHILDREN_XPATH_ENTIRE_LIST = ".//*";
    private final String webBrokerDropdownID = "td-wb-dropdown-account-selector";
    private final String wbBookValueFieldClass = "td-wb-holdings-di-totals__value td-wb-holdings-di-totals__value--book";
    private final By holdingTab = By.cssSelector("td-wb-tab[tdwbtabstatename=\"page.account.holdings\"]");

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
                WebElement dropDownCarret = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className(CARRET)));
                log.debug("Login successful. Proceeding to data collection.");
                if (driver.findElements(By.id("markAsReadAndDismiss")).size() > 0) {
                    click(driver.findElement(By.id("markAsReadAndDismiss")));
                    log.info("There was a message that had to be dismissed. Cleared this item so that we could proceed");
                }
                click(dropDownCarret);

                WebElement divOfAccounts = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("td-wb-dropdown__panel")));
                List<WebElement> accountDivs = divOfAccounts.findElements(By.xpath(CHILDREN_XPATH_ONE_DOWN));
                entries = collectData(accountDivs);
                iter = entries.iterator();
                if (driver != null) {
                    driver.quit();
                    driver = null;
                    log.debug("Quitting browser since no longer necessary to read entries. Everything collected from Web Broker.");
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
            } catch (ElementClickInterceptedException e){
                String message = "Flow interrupted due to an element being clicked. Throwing a runtime exception and quitting.";
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

    public TDWebBrokerItemReader(String userName, String password, AccountService accountService, String url) {

        this.userName = userName;
        this.password = password;
        this.accountService = accountService;
        this.webBrokerURL = url;
        if (userName == null || password == null)
            throw new RuntimeException("Credentials not provided. Crashing execution");

    }

    private List<AccountEntry> collectData (List<WebElement> accountDivs) throws InterruptedException {
        List<AccountEntry> entries= new ArrayList<>();
        var zero = new BigDecimal(0);
        var currentDate = LocalDateTime.now();
        for (int i = 0; i < accountDivs.size(); i++) {
            if (i != 0)
                click(driver.findElement(By.className(CARRET)));
            String dropdownRowId = "td-wb-dropdown-option-" + i;
            WebElement containerDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("td-wb-dropdown__panel")));
            click(containerDiv.findElement(By.id(dropdownRowId)));
            WebElement accountIdSpan = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("td-wb-account-label__number")));
            click(wait.until(ExpectedConditions.visibilityOfElementLocated(holdingTab)));
            AccountEntry accountEntry = new AccountEntry();

            Account acc = accountService.getAccountFromAccountNumber(accountIdSpan.getText());
            accountEntry.setAccount(acc);

            /* Need to wait 500ms because I need to wait for the elements to be updated and this looks to be done async.
             * If this isn't done, values will get attributed to the wrong account updated properly.
             *
             * There are ways of embedding a JS based option but I've avoided that for now.
             */
            Thread.sleep(500l);

            accountEntry.setMarketValue(getValue(By.className("td-wb-account-totals__total-balance-amount")));
            if (!accountEntry.getMarketValue().equals(zero)) {
                double bookVal = 0;
                try {
                    bookVal = getValue(By.cssSelector(".td-wb-holdings-di-totals__value--book"));
                } catch (NoSuchElementException e) {
                    //Do nothing. There are times when this is expected behaviour. The initial NaN assignment is expected.
                    //This happen when there's nothing in that particular account.
                }
                accountEntry.setBookValue(bookVal);
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

    private void assignAllocations (AccountEntry accountEntry) {
        var allocationMap = getAllAllocationContainers();
        var iter = allocationMap.entrySet().iterator();
        while (iter.hasNext()) {
            var entry = iter.next();
            var label = entry.getKey();
            var value = entry.getValue();
            switch (label) {
                case "Canadian Equities":
                    accountEntry.setCanadianEqtPct(value);
                    break;
                case "U.S. Equities":
                    accountEntry.setUsEqtPct(value);
                    break;
                case "International Equities":
                    //TODO need to break this down further since this is where Internation and Emerging Markets are set
                    accountEntry.setInternationalEqtPct(value);
                    break;
                case "Canadian Fixed Income":
                    accountEntry.setCadFixedIncomePct(value);
                    break;
                case "Global Fixed Income":
                    accountEntry.setGlobalFixedIncomePct(value);
                    break;
                case "Cash & Cash Equivalents":
                    accountEntry.setCashPct(value);
                    break;
                default:
                    accountEntry.setOtherPct(value);
                    break;
            }
        }

    }

    private void loginDriver () {
        driver.get(webBrokerURL);
        var byUserName100 = By.id("username100");
        var byUserName101 = By.id("username101");
        boolean useUserName100 = !driver.findElements(byUserName100).isEmpty();
        WebElement usernameField = useUserName100 ? driver.findElement(byUserName100) : driver.findElement(byUserName101);
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

    private double getValue (By by) throws NoSuchElementException{
        double result = 0;
        WebElement totalValueDiv = null;
        try {
            totalValueDiv = driver.findElement(by);
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

    /**
     * A bit of a backwards implementation. The container that houses all the allocations isn't easily searchable,
     * so I need to find one with a unique ID, then get it's parent, so that I can then traverse all the children
     */
    private Map<String, BigDecimal> getAllAllocationContainers () {
        By assetAllocationTab = By.cssSelector("td-wb-tab[tdwbtabstatename=\"page.account.asset-allocations\"]");
        WebElement we = wait.until(ExpectedConditions.visibilityOfElementLocated(assetAllocationTab));
        click(we.findElement(By.tagName("a")));

        Map<String, BigDecimal> allocationPctMap = new HashMap<>();
        WebElement cdnEqtDiv = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("td-wb-aria-asset-category-name-CANADIAN_EQUITIES")));
        WebElement allocationContainerDiv = cdnEqtDiv.findElement(By.xpath(PARENT_XPATH));
        List<WebElement> allocationDivs = allocationContainerDiv.findElements(By.xpath(CHILDREN_XPATH_ONE_DOWN));
        for (WebElement el : allocationDivs) {
            try {
                List<WebElement> elements = el.findElements(By.xpath(CHILDREN_XPATH_ENTIRE_LIST));
                String key = "";
                BigDecimal value = new BigDecimal(0.0);
                for (int i = 0; i < elements.size(); i++) {
                    WebElement elmnt = elements.get(i);
                    String className = elmnt.getAttribute("class");
                    if (className != null && className.contains("td-wb-asset-allocations__asset-class-name")) {
                        key = elmnt.getText();
                    } else if (elmnt.getText().trim().equals("% of Portfolio")) {
                        WebElement webElt = elements.get(i + 1);
                        String pctString = webElt.getText();
                        value = new BigDecimal(pctString.trim().replace("%", "")).divide(BigDecimal.valueOf(100));
                        break;
                    }
                }
                if (!"".equals(key))
                    allocationPctMap.put(key, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return allocationPctMap;
    }

    private double getBookValue () {
        String selector = ".td-wb-holdings-di-totals__value--book";
        return getValue(By.cssSelector(selector));
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
