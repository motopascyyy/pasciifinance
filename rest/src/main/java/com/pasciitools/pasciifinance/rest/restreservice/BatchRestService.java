package com.pasciitools.pasciifinance.rest.restreservice;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.exec.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public String invokeWebBrokerScraper() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
                .toJobParameters();
        JobExecution je = jobLauncher.run(loadWebBrokerDataFromChrome, jobParameters);
        String resultString = String.format ("Job %s completed. Status: %s", loadWebBrokerDataFromChrome.getName(), je.getStatus());
        return resultString;
    }

    @GetMapping("/scrapeEasyWeb")
    public String invokeEasyWebScraper() throws Exception {
        JobParameters jobParameters = new JobParametersBuilder().addLong("time", System.currentTimeMillis())
                .toJobParameters();
        JobExecution je = jobLauncher.run(loadEasyWebDataFromChrome, jobParameters);
        String resultString = String.format ("Job %s completed. Status: %s", loadEasyWebDataFromChrome.getName(), je.getStatus());
        return resultString;
    }

    @GetMapping("/pullAllData")
    public List<String> pullAllData () throws Exception {
        var results = new ArrayList<String>();
        results.add(invokeEasyWebScraper());
        results.add(invokeWebBrokerScraper());
        return results;
    }



}
