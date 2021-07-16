package com.pasciitools.pasciifinance.batch.seeding;

import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.service.AccountService;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AccountItemReader implements ItemReader<Account> {

    private AccountService accountService;

    private static final Logger log = LoggerFactory.getLogger(AccountItemReader.class);

    private List<Account> accounts;
    private int entryIndex;

    @Override
    public Account read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        Account nextEntry = null;
        if (entryIndex < accounts.size()){
            nextEntry = accounts.get(entryIndex);
            entryIndex++;
        } else {
            entryIndex = 0;
        }
        return nextEntry;
    }

    public AccountItemReader (String pathToFile, AccountService accountService) {
        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(pathToFile))){
            this.accountService = accountService;
            accounts = createAccountList(workbook);
        } catch (FileNotFoundException e) {
            log.error(String.format("Couldn't find the file: %s%nCause: %s.%nImpact: Unable to seed data into the DB from the desired file.", pathToFile, e.getMessage()), e);
        } catch (IOException e) {
            log.error(String.format("Generic IO Exception on file: %s%nCause: %s", pathToFile, e.getMessage()), e);
        }
    }

    private List<Account> createAccountList (Workbook workbook){
        var iter = workbook.sheetIterator();
        List<Account> accounts = new ArrayList<>();
        while (iter.hasNext()){
            Sheet sheet = iter.next();
            var account = createAccount(sheet);
            if (account != null)
                accounts.add(account);
        }
        return accounts;
    }

    private Account createAccount (Sheet sheet) {
        String sheetName = sheet.getSheetName().trim();
        if (!sheetName.toLowerCase().contains("summary")) {
            Account acc = new Account();
            String inst = accountService.getInstitutionFromSheetName(sheetName);
            acc.setInstitution(inst);
            acc.setAccountLabel(accountService.getAcctLabelFromSheetName(sheetName));
            if (sheetName.contains("TFSA"))
                acc.setAccountType("TFSA");
            else if (sheetName.contains("RSP"))
                acc.setAccountType("RSP");
            else
                acc.setAccountType("Taxable");

            if (acc.getAccountLabel().toLowerCase().contains("mutual") || acc.getInstitution().toLowerCase().contains("tangerine"))
                acc.setActive(false);
            else
                acc.setActive(true);

            if (acc.getAccountLabel().toLowerCase().contains("joint"))
                acc.setJointAccount(true);
            else
                acc.setJointAccount(false);
            if (log.isDebugEnabled())
                log.debug(acc.toString());

            return acc;
        }
        return null;
    }
}
