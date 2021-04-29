package com.pasciitools.pasciifinance.batch.seeding;

import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.service.AccountService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AccountItemReader implements ItemReader<Account> {

    @Autowired
    private AccountService accountService;

    private static final Logger log = LoggerFactory.getLogger(AccountItemReader.class);

    private List<Account> accounts;
    @Override
    public Account read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        return null;
    }

    public AccountItemReader (String pathToFile) {
        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(pathToFile))){
            accounts = createAccountList(workbook);
        } catch (FileNotFoundException e) {
            log.error(String.format("Couldn't find the file: %s%nbecause of: %s", pathToFile, e.getMessage()), e);
        } catch (IOException e) {
            log.error(String.format("Generic IO Exception on file: %s%nbecause of: %s", pathToFile, e.getMessage()), e);
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
