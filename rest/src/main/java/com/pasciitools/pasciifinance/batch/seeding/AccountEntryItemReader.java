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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Iterator;

public class AccountEntryItemReader implements ItemReader<AccountEntry> {

    private final AccountService accountService;

    private static final Logger log = LoggerFactory.getLogger(AccountEntryItemReader.class);

    private String pathToFile;

    private Workbook workbook;

    public AccountEntryItemReader (String pathToFile, AccountService accountService) {
        this.accountService = accountService;
        this.pathToFile = pathToFile;
    }

    private AccountEntry createEntryFromRow (Row row, Account account) {
        String sheetName = row.getSheet().getSheetName();
        AccountEntry ae = null;
        try {
            var dateCell = row.getCell(0);
            var bookValueCell = row.getCell(1);
            var marketValueCell = row.getCell(2);
            if (dateCell == null || dateCell.getDateCellValue() == null) {
                if (log.isDebugEnabled())
                    log.debug(String.format("Exiting sheet %s at %o", sheetName, row.getRowNum()));
            } else {
                ae = new AccountEntry();
                ae.setAccount(account);
                var entryDate = dateCell.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                double bookValue = bookValueCell != null ? bookValueCell.getNumericCellValue() : marketValueCell.getNumericCellValue();
                double marketValue = marketValueCell.getNumericCellValue();
                ae.setEntryDate(entryDate);
                ae.setBookValue(bookValue);
                ae.setMarketValue(marketValue);
            }
        } catch (Exception e) {
            log.error("ERROR reading from cell", e);
            log.error(String.format("Failed to get the expected value for cell 0: %s",row.getCell(0)));
            log.error(String.format("Failed to get the expected value for cell 1: %s",row.getCell(1)));
            log.error(String.format("Failed to get the expected value for cell 2: %s",row.getCell(2)));
        }
        return ae;
    }

    private AccountEntry getNextRow () {
        AccountEntry result;
        do {
            Row row = null;
            if (currentSheet == null && worksheetIterator.hasNext()) {
                currentSheet = worksheetIterator.next();
                if ("summary".equalsIgnoreCase(currentSheet.getSheetName()))
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
            var acc = accountService.getAccountFromSheetName(currentSheet.getSheetName());
            result = row!= null ? createEntryFromRow(row, acc) : null;
        } while (result == null && (rowIterator.hasNext() || worksheetIterator.hasNext()));
        return result;
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
            log.error(String.format("Couldn't find the file:%s%n because of: %s", pathToFile, e.getMessage()), e);
        } catch (IOException e) {
            log.error(String.format("Generic IO Exception on file:%s%n because of: %s", pathToFile, e.getMessage()), e);
        }

        return result;
    }
}
