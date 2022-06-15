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

    private static final String TWO_FA_DIALOG_ID = "mat-dialog-0";
    private final NumberFormat nfCAD = NumberFormat.getCurrencyInstance(Locale.CANADA);
    protected WebDriver driver;
    protected WebDriverWait wait;

    protected SharedWebDriver sharedWebDriver;


}
