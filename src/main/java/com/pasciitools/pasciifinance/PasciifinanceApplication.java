package com.pasciitools.pasciifinance;

import com.pasciitools.pasciifinance.account.Account;
import com.pasciitools.pasciifinance.account.AccountEntry;
import com.pasciitools.pasciifinance.account.AccountEntryRepository;
import com.pasciitools.pasciifinance.account.AccountRepository;
import com.pasciitools.pasciifinance.batch.ExcelFileDataLoader;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;

import java.util.List;

@SpringBootApplication
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
			if (args.length > 0 && !args[0].equals("-skipLoad")) {
				System.out.println(args[0]);
				ExcelFileDataLoader loader = new ExcelFileDataLoader(pathToInitLoaderFile, accountRepo);

				List<Account> accounts = loader.parseForAccounts();
				try {
					accountRepo.saveAll(accounts);
				} catch (Exception e) {
					log.error("Couldn't new accounts because of a key constraint: " + e.getMessage(), e);
				}
				List<AccountEntry> entries = loader.parseForEntries();
//			entryRepo.saveAll(entries);
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
