package com.pasciitools.pasciifinance.batch.seeding;

import com.pasciitools.pasciifinance.batch.JobCompletionNotificationListener;
import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.repository.AccountEntryRepository;
import com.pasciitools.pasciifinance.common.repository.AccountRepository;
import com.pasciitools.pasciifinance.common.service.AccountService;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableBatchProcessing
@EnableJpaRepositories(basePackages = {"com.pasciitools.pasciifinance.common.repository"})
@ComponentScan(basePackages = {"com.pasciitools.pasciifinance.common.repository"})
@EntityScan(basePackages = {"com.pasciitools.pasciifinance.common.entity"})
public class ExcelSeedBatchConfiguration {

    @Value("${pathToInitLoaderFile}")
    private String pathToInitLoaderFile;

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    private AccountEntryRepository aeRepo;
    @Autowired
    private AccountRepository accRepo;

    @Autowired
    private AccountService accountService;


    /**
     * First block of readers, writers and processors to handle the accounts. This is require
     * before we handle the individual entries in each account
     */

    @Bean
    public AccountItemReader accountReader() {
        return new AccountItemReader(pathToInitLoaderFile, accountService);
    }

    @Bean
    public RepositoryItemWriter<Account> accountWriter() {
        return new RepositoryItemWriterBuilder<Account>()
                .repository(accRepo).methodName("save").build();
    }

    /**
     * Second block of readers, writers and processors to handle the account entries. This is require
     * before we handle the individual entries in each account
     */

    @Bean
    public AccountEntryItemReader entryReader() {
        return new AccountEntryItemReader(pathToInitLoaderFile, accountService);
    }

    @Bean
    public RepositoryItemWriter<AccountEntry> entryWriter() {
        return new RepositoryItemWriterBuilder<AccountEntry>()
                .repository(aeRepo).methodName("save").build();
    }


    @Bean
    public Job importEntriesFromExcelJob(JobCompletionNotificationListener listener, Step loadAccounts, Step loadEntries) {
        return jobBuilderFactory.get("importEntriesFromExcelJob").incrementer(new RunIdIncrementer()).listener(listener).start(loadAccounts).next(loadEntries).build();
    }

    @Bean
    public Step loadAccounts (RepositoryItemWriter<Account> writer) {
        return stepBuilderFactory.get("loadAccounts")
                .<Account, Account> chunk(10)
                .reader(accountReader())
                .writer(writer)
                .build();
    }

    @Bean Step loadEntries (RepositoryItemWriter<AccountEntry> writer) {
        return stepBuilderFactory.get("loadEntries")
                .<AccountEntry, AccountEntry> chunk(10)
                .reader(entryReader())
                .writer(writer)
                .build();
    }
}
