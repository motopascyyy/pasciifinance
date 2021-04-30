package com.pasciitools.pasciifinance.batch;

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

    @Autowired
    public JobCompletionNotificationListener (AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("!!! JOB FINISHED! Time to verify the results");
            int totalNumberOfAccounts = accountService.getNumberOfAccounts();
            log.info(String.format("Verified that %s accounts were inserted into the DB.", totalNumberOfAccounts));
        }
    }
}
