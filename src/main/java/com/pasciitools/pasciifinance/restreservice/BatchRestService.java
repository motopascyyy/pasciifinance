package com.pasciitools.pasciifinance.restreservice;

import com.pasciitools.pasciifinance.batch.Site;
import com.pasciitools.pasciifinance.batch.tdcrawler.SharedWebDriver;
import com.pasciitools.pasciifinance.common.configuration.TDConfig;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@RestController
public class BatchRestService {
    private static final Logger log = LoggerFactory.getLogger(BatchRestService.class);

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    private Job importEntriesFromExcelJob;

    @Autowired
    private Job loadWebBrokerDataFromChrome;

    @Autowired
    private Job loadEasyWebDataFromChrome;

    @Autowired
    private TDConfig tdConfig;


    @GetMapping("/excelImportBatchJob")
    public String invokeExcelImportBatchJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(importEntriesFromExcelJob, jobParameters);
        return String.format("Batch job has been invoked");
    }

    @GetMapping("/scrapeWebBroker")
    public String invokeWebBrokerScraper()  {
        try {
            return scrapeWebBroker();
        } catch (JobExecutionException e){
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, e.getMessage(), e);
        }
    }

    private String scrapeWebBroker () throws JobExecutionException {
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
                .toJobParameters();
        JobExecution je = jobLauncher.run(loadWebBrokerDataFromChrome, jobParameters);
        String resultString = String.format ("Job %s completed. Status: %s", loadWebBrokerDataFromChrome.getName(), je.getStatus());
        if (je.getStatus() == BatchStatus.FAILED) {
            throw new JobExecutionException("Failed to scrape TD Web Broker because");
        }
        return resultString;
    }

    @GetMapping("/scrapeEasyWeb")
    public String invokeEasyWebScraper() throws Exception {
        try {
            return scrapeEasyWeb();
        } catch (JobExecutionException e){
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, e.getMessage(), e);
        }
    }

    private String scrapeEasyWeb () throws JobExecutionException {
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
                .toJobParameters();
        JobExecution je = jobLauncher.run(loadEasyWebDataFromChrome, jobParameters);
        String resultString = String.format ("Job %s completed. Status: %s", loadEasyWebDataFromChrome.getName(), je.getStatus());
        if (je.getStatus() == BatchStatus.FAILED) {
            throw new JobExecutionException("Failed to scrape TD Web Broker because");
        }
        return resultString;
    }

    @GetMapping("/pullAllData")
    public ResponseEntity<String> pullAllData () {
        var results = new ArrayList<String>();
        var failureOccurred = false;
        var failureMessage = "";
        try {
            initiateBrowserDriver();
            loginDriver(Site.TD_EASYWEB);
        } catch (Exception e) {
            failureOccurred = true;
            failureMessage =String.format("Login failed due to: %s", e.getMessage());
            log.error( failureMessage, e);
        }

        try {
            if (!failureOccurred)
                results.add(scrapeEasyWeb());
        } catch (Exception e) {
            failureOccurred = true;
            failureMessage =String.format("Failed to scraping for EasyWeb due to: %s", e.getMessage());
            log.error( failureMessage, e);
        }
        try {
            if (!failureOccurred)
                results.add(scrapeWebBroker());
        } catch (JobExecutionException e){
            failureOccurred = true;
            failureMessage =String.format("Failed to scraping for WebBroker due to: %s", e.getMessage());
            log.error( failureMessage, e);
        }

        SharedWebDriver.getInstance().killDriver();
        if (failureOccurred) {
            return ResponseEntity.internalServerError().body(failureMessage);
        } else {
            return ResponseEntity.ok().body("All reads successful");
        }
    }


    @PostMapping("/initiateBrowserDriver")
    public ResponseEntity<String> initiateBrowserDriver () {
        WebDriver sharedDriver = SharedWebDriver.getInstance().getDriver();
        if (sharedDriver != null)
            return ResponseEntity.ok().body("Shared Web Driver started.");
        else {
            return ResponseEntity.internalServerError().body("Could not start SharedWebDriver");
        }
    }

    @GetMapping("/login-to-td")
    public ResponseEntity<String> loginDriver (@RequestParam Site site) {

//        SharedWebDriver.getInstance().loginDriver(site, username, password);
        String url;
        String successUrl;
        String username;
        String password;
        switch (site) {
            case TD_EASYWEB:
                url = tdConfig.getEasywebUrl();
                successUrl = tdConfig.getEasywebSuccessLoginUrl();
                username = tdConfig.getEasywebUserName();
                password = tdConfig.getEasywebPassword();
                break;
            case TD_WEBBROKER:
                url = tdConfig.getWebbrokerUrl();
                successUrl = tdConfig.getWebbrokerSuccessLoginUrl();
                username = tdConfig.getWebbrokerUserName();
                password = tdConfig.getWebbrokerPassword();
                break;
            default:
                url = tdConfig.getEasywebUrl();
                successUrl = tdConfig.getEasywebSuccessLoginUrl();
                username = tdConfig.getEasywebUserName();
                password = tdConfig.getEasywebPassword();
                break;
        }
        SharedWebDriver.getInstance().loginDriver(username,password, url, successUrl, tdConfig.getUsernameFieldOptions().split(","));
        return ResponseEntity.ok().body("Successfully logged into " + site);

    }

    @GetMapping("/enter2FA")
    public ResponseEntity<String> enter2FA (@RequestParam String twoFA) {
        return ResponseEntity.internalServerError().body("Failed to complete 2FA insertion");
    }
}
