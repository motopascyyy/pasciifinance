package com.pasciitools.pasciifinance.batch.seeding;


import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.service.AccountService;
import org.apache.poi.ss.usermodel.Cell;
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
import java.util.*;

public class AccountEntryItemReader implements ItemReader<AccountEntry> {

    private AccountService accountService;

    private static final Logger log = LoggerFactory.getLogger(AccountEntryItemReader.class);

    private List<AccountEntry> entries;
    private int entryIndex = 0;

    public AccountEntryItemReader (String pathToFile, AccountService accountService) {
        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(pathToFile))){
            this.accountService = accountService;
            entries = new ArrayList<>();
            Iterator<Sheet> iter = workbook.sheetIterator();
            while (iter.hasNext()) {
                Sheet sheet = iter.next();
                String sheetName = sheet.getSheetName();
                if (!"summary".equals(sheetName.toLowerCase())){
                    var accountEntries = createAccountEntryList(sheet);
                    entries.addAll(accountEntries);
                }
            }
            log.info(String.format("%s account entries read from file. Validate that many records are saved.", entries.size()));
        } catch (FileNotFoundException e) {
            log.error(String.format("Couldn't find the file: %s%nbecause of: %s", pathToFile, e.getMessage()), e);
        } catch (IOException e) {
            log.error(String.format("Generic IO Exception on file: %s%nbecause of: %s", pathToFile, e.getMessage()), e);
        }
    }

    private List<AccountEntry> createAccountEntryList (Sheet sheet){
        List<AccountEntry> accountEntries = new ArrayList<>();
        String sheetName = sheet.getSheetName();
        Account account = accountService.getAccountFromSheetName(sheetName);
        for (Row row : sheet){
            if (row.getRowNum() != 0) {
                AccountEntry ae = createEntryFromRow(row, account);
                if (ae == null)
                    break;
                accountEntries.add(ae);
            }
        }
        return accountEntries;
    }

    private AccountEntry createEntryFromRow (Row row, Account account) {
        String sheetName = row.getSheet().getSheetName();
        AccountEntry ae = null;
        Cell dateCell = row.getCell(0);
        Cell bookValueCell = row.getCell(1);
        Cell marketValueCell = row.getCell(2);
        if (dateCell == null || dateCell.getDateCellValue() == null) {
            if (log.isDebugEnabled())
                log.debug(String.format("Exiting sheet %s at %o", sheetName, row.getRowNum()));
        } else {
            ae = new AccountEntry();
            ae.setAccount(account);
            Date entryDate = dateCell.getDateCellValue();
            double bookValue = bookValueCell != null ? bookValueCell.getNumericCellValue() : marketValueCell.getNumericCellValue();
            double marketValue = marketValueCell.getNumericCellValue();
            ae.setEntryDate(entryDate);
            ae.setBookValue(bookValue);
            ae.setMarketValue(marketValue);
        }
        return ae;
    }

    @Override
    public AccountEntry read() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        AccountEntry nextEntry = null;
        if (entryIndex < entries.size()){
            nextEntry = entries.get(entryIndex);
            entryIndex++;
        } else {
            entryIndex = 0;
        }
        return nextEntry;
    }
}
