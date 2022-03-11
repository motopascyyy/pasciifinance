package com.pasciitools.pasciifinance.restreservice;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.exec.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    public List<String> pullAllData () {
        var results = new ArrayList<String>();
        try {
            results.add(scrapeEasyWeb());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, e.getMessage(), e);
        }
        try {
            results.add(scrapeWebBroker());
        } catch (JobExecutionException e){
            throw new ResponseStatusException(HttpStatus.FAILED_DEPENDENCY, e.getMessage(), e);
        }
        return results;
    }



}
