package com.pasciitools.pasciifinance;

import com.pasciitools.pasciifinance.batch.ExcelFileDataLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class PasciifinanceApplication {

	@Value("${pathToInitLoaderFile}")
	private String pathToInitLoaderFile;

	private static final Logger log = LoggerFactory.getLogger(PasciifinanceApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(PasciifinanceApplication.class, args);

	}

	@Bean
	public CommandLineRunner processAccounts(AccountRepository accountRepo, AccountEntryRepository entryRepo) {
		return (args) -> {
			ExcelFileDataLoader loader = new ExcelFileDataLoader(pathToInitLoaderFile, accountRepo);

			List<Account> accounts = loader.parseForAccounts();
			accountRepo.saveAll(accounts);
			List<AccountEntry> entries = loader.parseForEntries();
			entryRepo.saveAll(entries);
			loader.closeAll();

			List<AccountEntry> latestEntries = entryRepo.getLatestResults(new Date());
			for (AccountEntry ae : latestEntries) {
				log.info(ae.toString());
			}

			SimpleDateFormat ft = new SimpleDateFormat ("yyyy-MM-dd");
			Date d = ft.parse("2021-01-16");
			log.info("Getting entries up until " + d);
			List<AccountEntry> latestEntriesToJan16 = entryRepo.getLatestResults(d);
			for (AccountEntry ae : latestEntriesToJan16) {
				log.info(ae.toString());
			}
		};
	}


}
