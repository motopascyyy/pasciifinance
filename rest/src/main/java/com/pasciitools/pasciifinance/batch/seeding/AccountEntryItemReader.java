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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class AccountEntryItemReader implements ItemReader<AccountEntry> {

    private AccountService accountService;

    private static final Logger log = LoggerFactory.getLogger(AccountEntryItemReader.class);

    private List<AccountEntry> entries;
    private int entryIndex = 0;
    private String pathToFile;


//    public AccountEntryItemReader (String pathToFile, AccountService accountService) {
//        //TODO move this logic into the "read" method. It doesn't belong in the constructor.
//        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(pathToFile))){
//            this.accountService = accountService;
//            this.pathToFile = pathToFile;
//            entries = new ArrayList<>();
//            Iterator<Sheet> iter = workbook.sheetIterator();
//            while (iter.hasNext()) {
//                Sheet sheet = iter.next();
//                String sheetName = sheet.getSheetName();
//                if (!"summary".equals(sheetName.toLowerCase())){
//                    var accountEntries = createAccountEntryList(sheet);
//                    entries.addAll(accountEntries);
//                }
//            }
//        } catch (FileNotFoundException e) {
//            log.error(String.format("Couldn't find the file: %s%nbecause of: %s", pathToFile, e.getMessage()), e);
//        } catch (IOException e) {
//            log.error(String.format("Generic IO Exception on file: %s%nbecause of: %s", pathToFile, e.getMessage()), e);
//        }
//    }

    private Workbook workbook;

    public AccountEntryItemReader (String pathToFile, AccountService accountService) {
        this.accountService = accountService;
        this.pathToFile = pathToFile;
    }

    private List<AccountEntry> createAccountEntryList (Sheet sheet) throws IOException {
        List<AccountEntry> accountEntries = new ArrayList<>();
        String sheetName = sheet.getSheetName();
        Account account = accountService.getAccountFromSheetName(sheetName);
        if (account == null)
            throw new IOException(String.format("No Account found for sheet name: %s",sheetName));
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
        try {
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
        } catch (Exception e) {
            log.error("ERROR reading from cell", e);
            log.error(String.format("Failed to get the exepcted value for cell 0: %s",row.getCell(0)));
            log.error(String.format("Failed to get the exepcted value for cell 1: %s",row.getCell(1)));
            log.error(String.format("Failed to get the exepcted value for cell 2: %s",row.getCell(2)));
        }
        return ae;
    }

    private AccountEntry getNextRow () {
        AccountEntry result = null;
        do {
            Row row = null;
            if (currentSheet == null && worksheetIterator.hasNext()) {
                currentSheet = worksheetIterator.next();
                if ("summary".equals(currentSheet.getSheetName().toLowerCase()))
                    currentSheet = worksheetIterator.next();
                rowIterator = currentSheet.rowIterator();
                if (rowIterator.hasNext()) {
                    rowIterator.next(); //skip the first row because it's a header row (fugly, I know)
                    row = rowIterator.next();
                }
            }
            // if we're dealing with a current sheet, but the next row is empty
            else if (currentSheet != null && !rowIterator.hasNext()) {
                if (worksheetIterator.hasNext()) {
                    currentSheet = worksheetIterator.next();
                    rowIterator = currentSheet.rowIterator();
                    if (rowIterator.hasNext()) {
                        rowIterator.next(); //skip the first row because it's a header row (fugly, I know)
                        row = rowIterator.next();
                    }
                }
            }
            //if we're dealing with a current sheet and the next row isn't empty, get the next row
            else if (currentSheet != null && rowIterator.hasNext()) {
                row = rowIterator.next();
            }
            Account acc = accountService.getAccountFromSheetName(currentSheet.getSheetName());
            result = row!= null ? createEntryFromRow(row, acc) : null;
        } while (result == null && (rowIterator.hasNext() || worksheetIterator.hasNext()));
        return result;
    }

    public AccountEntry oldRead() throws UnexpectedInputException, ParseException, NonTransientResourceException {
        AccountEntry nextEntry = null;
        if (entryIndex < entries.size()){
            nextEntry = entries.get(entryIndex);
            entryIndex++;
        } else {
            entryIndex = 0;
        }
        return nextEntry;
    }

    private Iterator<Row> rowIterator;
    private Iterator<Sheet> worksheetIterator;
    private Sheet currentSheet;

    @Override
    public AccountEntry read() throws UnexpectedInputException, ParseException, NonTransientResourceException {

        AccountEntry result = null;
        try {

            if (workbook == null) {
                workbook = new XSSFWorkbook(new FileInputStream(pathToFile));
                worksheetIterator = workbook.sheetIterator();
                log.info("Iterator initiated to read from file");
            }
            //If we're dealing with the first row of the first sheet, means the worksheet will be empty but the iterator
            //will have been initialized and ready with a hasNext()
            result = getNextRow();
            if (result == null) {
                //if we get here, it must mean we're at the end of the file, so close it up to avoid memory leaks.
                currentSheet = null;
                workbook.close();
                workbook = null;
            }
        } catch (FileNotFoundException e) {
            log.error(String.format("Couldn't find the file: %s%nbecause of: %s", pathToFile, e.getMessage()), e);
        } catch (IOException e) {
            log.error(String.format("Generic IO Exception on file: %s%nbecause of: %s", pathToFile, e.getMessage()), e);
        }

        return result;
    }
}
