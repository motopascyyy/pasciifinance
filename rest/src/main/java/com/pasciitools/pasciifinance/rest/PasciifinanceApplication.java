package com.pasciitools.pasciifinance.rest;

import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.repository.AccountEntryRepository;
import com.pasciitools.pasciifinance.common.repository.AccountRepository;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import java.util.List;

import org.springframework.context.annotation.ComponentScan;
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

	@Bean
	public CommandLineRunner processAccounts() {
		return (args) -> {
			boolean seedDB = false;
			if (args.length > 0 ) {
				for (String arg : args) {
					if (arg.equals("-seedDB")) {
						seedDB = true;
						break;
					}
				}
			}
			if (seedDB) {
				if (log.isDebugEnabled())
					log.debug(String.format("Seeding DB with data from: %s", pathToInitLoaderFile));
				if (pathToInitLoaderFile == null || pathToInitLoaderFile.trim().length() == 0) {
					log.error("Trying to seed data into the DB but property \"pathToInitLoaderFile\" in an application.properties " +
							"file that's accessible. " +
							"Please check that the file is included in the classpath or that the property has a value");
					System.exit(1);
				}
				ExcelFileDataLoader loader = new ExcelFileDataLoader(pathToInitLoaderFile, accountRepo);

				List<Account> accounts = loader.parseForAccounts();
				try {
					accountRepo.saveAll(accounts);
				} catch (Exception e) {
					log.error("Couldn't new accounts because of a key constraint: " + e.getMessage(), e);
				}
				List<AccountEntry> entries = loader.parseForEntries();
				for (AccountEntry entry : entries) {
					try {
						entryRepo.save(entry);
					} catch (Exception e) {
						log.error("Couldn't save account entry: " + entry.toString(), e);
					}
				}
				loader.closeAll();
				log.info("All Data Loaded. Ready to handle requests.");
			}
			log.info("Ready to process requests");
		};
	}


}
