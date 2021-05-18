package com.pasciitools.pasciifinance.rest;

import com.pasciitools.pasciifinance.common.repository.AccountEntryRepository;
import com.pasciitools.pasciifinance.common.repository.AccountRepository;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EnableEncryptableProperties
@EnableJpaRepositories(basePackages="com.pasciitools.pasciifinance")
@EntityScan(basePackages = "com.pasciitools.pasciifinance")
@SpringBootApplication(scanBasePackages = "com.pasciitools.pasciifinance.*")
public class PasciifinanceApplication {

	@Value("${pathToInitLoaderFile}")
	private String pathToInitLoaderFile;

	@Autowired
	private AccountRepository accountRepo;

	@Autowired
	private AccountEntryRepository entryRepo;

	private static final Logger log = LoggerFactory.getLogger(PasciifinanceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(PasciifinanceApplication.class, args);

	}
}
