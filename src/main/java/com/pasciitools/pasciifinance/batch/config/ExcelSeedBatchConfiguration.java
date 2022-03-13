package com.pasciitools.pasciifinance.batch.config;

import com.pasciitools.pasciifinance.batch.listener.JobCompletionNotificationListener;
import com.pasciitools.pasciifinance.batch.seeding.AccountEntryItemReader;
import com.pasciitools.pasciifinance.batch.seeding.AccountItemReader;
import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.service.AccountEntryService;
import com.pasciitools.pasciifinance.common.service.AccountService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {"com.pasciitools.pasciifinance.common.repository"})
@ComponentScan(basePackages = {"com.pasciitools.pasciifinance.*"})
@EntityScan(basePackages = {"com.pasciitools.pasciifinance"})
@Configuration
@EnableBatchProcessing
public class ExcelSeedBatchConfiguration {

    @Value("${pathToInitLoaderFile}")
    private String pathToInitLoaderFile;

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountEntryService entryService;

    @Bean
    public JobExecutionListener listener() {
        return new JobCompletionNotificationListener(accountService, entryService);
    }


    @Bean
    public Job importEntriesFromExcelJob(JobCompletionNotificationListener listener) {
        return jobBuilderFactory.get("importEntriesFromExcelJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .start(loadAccounts())
                .next(loadEntries())
                .build();
    }

    @Bean
    public Step loadAccounts () {
        var accountReader = new AccountItemReader(pathToInitLoaderFile, accountService);
        var writer = new RepositoryItemWriterBuilder<Account>()
                .repository(accountService.getAcctRepo()).methodName("save").build();
        return stepBuilderFactory.get("loadAccounts")
                .<Account, Account> chunk(10)
                .reader(accountReader)
                .writer(writer)
                .build();
    }

    @Bean
    public Step loadEntries () {
        var entryReader = new AccountEntryItemReader(pathToInitLoaderFile, accountService);
        var entryWriter = new RepositoryItemWriterBuilder<AccountEntry>()
                .repository(entryService.getEntryRepo()).methodName("save").build();
        return stepBuilderFactory.get("loadEntries")
                .<AccountEntry, AccountEntry> chunk(10)
                .reader(entryReader)
                .writer(entryWriter)
                .build();
    }

}
