package com.pasciitools.pasciifinance.batch.listener;

import com.pasciitools.pasciifinance.common.service.AccountEntryService;
import com.pasciitools.pasciifinance.common.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotificationListener extends JobExecutionListenerSupport {
    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);

    private AccountService accountService;
    private AccountEntryService entryService;

    @Autowired
    public JobCompletionNotificationListener (AccountService accountService, AccountEntryService entryService) {
        log.info(String.format("JOBCOMPLETIONNOTIFICATIONLISTENER created: %s", this.hashCode()));
        this.accountService = accountService;
        this.entryService = entryService;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info(String.format("AFTER JOB - Status: %s", jobExecution.getStatus()));
        if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("!!! JOB FINISHED! Time to verify the results");
            log.info(String.format("%s accounts found in the DB.", accountService.getTotalNumberOfAccounts()));
            log.info(String.format("%s account entries found in the DB.", entryService.getTotalNumberOfEntries()));

        }
    }
}
