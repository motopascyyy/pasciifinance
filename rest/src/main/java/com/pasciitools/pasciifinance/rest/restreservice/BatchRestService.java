package com.pasciitools.pasciifinance.rest.restreservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BatchRestService {
    private static final Logger log = LoggerFactory.getLogger(BatchRestService.class);

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    private Job importEntriesFromExcelJob;

    @Autowired
    private Job loadWebBrokerDataFromChrome;

    @GetMapping("/excelImportBatchJob")
    public String invokeExcelImportBatchJob() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(importEntriesFromExcelJob, jobParameters);
        return String.format("Batch job has been invoked");
    }

    @GetMapping("/scrapeWebBroker")
    public String invokeWebBrokerScraper() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
                .toJobParameters();
        jobLauncher.run(loadWebBrokerDataFromChrome, jobParameters);
        return String.format("Batch job has been invoked");
    }



}
