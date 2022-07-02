package com.pasciitools.pasciifinance.batch.config;

import com.pasciitools.pasciifinance.batch.tdcrawler.TDEasyWebItemReader;
import com.pasciitools.pasciifinance.batch.listener.JobCompletionNotificationListener;
import com.pasciitools.pasciifinance.common.configuration.TDConfig;
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
public class TDEasyWebConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Autowired
    private AccountEntryRepository accountEntryRepository;
    @Autowired
    private AccountRepository accRepo;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TDConfig tdConfig;


    @Bean
    public TDEasyWebItemReader easyWebReader() {
        return new TDEasyWebItemReader(accountService, tdConfig);
    }

    @Bean
    public RepositoryItemWriter<AccountEntry> wbEntryWriter() {
        return new RepositoryItemWriterBuilder<AccountEntry>()
                .repository(accountEntryRepository).methodName("save").build();
    }

    @Bean
    public Job loadEasyWebDataFromChrome(JobCompletionNotificationListener listener, Step parseEasyWebEntries) {
        return jobBuilderFactory.get("loadEasyWebDataFromChrome")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(parseEasyWebEntries)
                .end()
                .build();
    }

    @Bean
    public Step parseEasyWebEntries(RepositoryItemWriter<AccountEntry> easyWebEntryWriter) {
        return stepBuilderFactory.get("parseEasyWebEntries")
                .<AccountEntry, AccountEntry>chunk(10)
                .reader(easyWebReader())
                .writer(easyWebEntryWriter)
                .build();
    }
}
