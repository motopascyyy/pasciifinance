package com.pasciitools.pasciifinance.batch.config;

import com.pasciitools.pasciifinance.batch.listener.JobCompletionNotificationListener;
import com.pasciitools.pasciifinance.batch.tdcrawler.TDWebBrokerItemReader;
import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.repository.AccountEntryRepository;
import com.pasciitools.pasciifinance.common.repository.AccountRepository;
import com.pasciitools.pasciifinance.common.service.AccountService;
import com.pasciitools.pasciifinance.common.service.SecurityService;
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
public class TDWebBrokerConfiguration {

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
    private SecurityService securityService;

    @Value("${webbroker.userName}")
    private String userName;

    @Value("${webbroker.password}")
    private String password;

    @Value("${webbroker.url}")
    private String webBrokerUrl;

    @Bean
    public TDWebBrokerItemReader reader() {
        return new TDWebBrokerItemReader(userName, password, accountService, securityService, webBrokerUrl);
    }

    @Bean
    public RepositoryItemWriter<AccountEntry> wbEntryWriter() {
        return new RepositoryItemWriterBuilder<AccountEntry>()
                .repository(accountEntryRepository).methodName("save").build();
    }

    @Bean
    public Job loadWebBrokerDataFromChrome(JobCompletionNotificationListener listener, Step parseEntries) {
        return jobBuilderFactory.get("loadWebBrokerDataFromChrome")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(parseEntries)
                .end()
                .build();
    }

    @Bean
    public Step parseEntries(RepositoryItemWriter<AccountEntry> wbEntryWriter) {
        return stepBuilderFactory.get("parseEntries")
                .<AccountEntry, AccountEntry>chunk(10)
                .reader(reader())
                .writer(wbEntryWriter)
                .build();
    }
}
