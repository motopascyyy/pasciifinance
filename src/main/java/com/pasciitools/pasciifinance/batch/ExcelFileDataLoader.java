package com.pasciitools.pasciifinance.batch;

import com.pasciitools.pasciifinance.Account;
import com.pasciitools.pasciifinance.AccountEntry;
import com.pasciitools.pasciifinance.AccountEntryRepository;
import com.pasciitools.pasciifinance.AccountRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.util.*;

public class ExcelFileDataLoader {

    private static final Logger log = LoggerFactory.getLogger(ExcelFileDataLoader.class);

    private String pathToFile;
    private Workbook workbook;
    private FileInputStream fis;
    private AccountRepository acctRepo;


    public ExcelFileDataLoader(String pathToInitLoaderFile, AccountRepository repo) {
        this.pathToFile = pathToInitLoaderFile;
        try {
            fis = new FileInputStream(new File(pathToFile));
            acctRepo = repo;
            workbook = new XSSFWorkbook(fis);
        } catch (FileNotFoundException e) {
            log.error(String.format("Couldn't find the file: %s\nbecause of: %s", pathToFile, e.getMessage()), e);
        } catch (IOException e) {
            log.error(String.format("Generic IO Exception on file: %s\nbecause of: %s", pathToFile, e.getMessage()), e);
        }
    }

    public List<Account> parseForAccounts() {
        List<Account> accounts = new ArrayList<Account>();
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            Account acc = new Account();
            String sheetName = workbook.getSheetName(i).trim();
            if (!sheetName.toLowerCase().contains("summary")) {
                String inst = getInstitutionFromSheetName(sheetName);
                acc.setInstitution(inst);
                acc.setAccountLabel(getAcctLabelFromSheetName(sheetName));
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

                accounts.add(acc);
                log.info(acc.toString());
            }
        }
        return accounts;
    }

    public List<AccountEntry> parseForEntries() {
        List<AccountEntry> entries = new ArrayList<AccountEntry>();
        Iterator<Sheet> iter = workbook.sheetIterator();
        while (iter.hasNext()) {
            Sheet sheet = iter.next();
            String sheetName = sheet.getSheetName();
            if (!sheetName.toLowerCase().contains("summary")) {
                Long accountId = getAcctId(sheetName);
                for (Row row : sheet) {
                    if (row.getRowNum() != 0) {
                        AccountEntry ae = new AccountEntry();
                        ae.setAccountId(accountId);
                        Cell dateCell = row.getCell(0);
                        Cell bookValueCell = row.getCell(1);
                        Cell marketValueCell = row.getCell(2);
                        if (dateCell == null || dateCell.getDateCellValue() == null) {
                            log.info(String.format("Exiting sheet %s at %o", sheetName, row.getRowNum()));
                            break;
                        }
                        Date entryDate = dateCell.getDateCellValue();
                        double bookValue = bookValueCell != null ? bookValueCell.getNumericCellValue() : marketValueCell.getNumericCellValue();
                        double marketValue = marketValueCell.getNumericCellValue();
                        ae.setEntryDate(entryDate);
                        ae.setBookValue(bookValue);
                        ae.setMarketValue(marketValue);
                        entries.add(ae);
                    }
                }
            }
        }
        return entries;
    }

    private String getInstitutionFromSheetName (String sheetName) {
        return sheetName.split(" ")[0].trim();
    }

    private String getAcctLabelFromSheetName (String sheetName) {
        String inst = getInstitutionFromSheetName(sheetName);
        return sheetName.substring((inst.length())).trim();
    }

    private Long getAcctId (String sheetName) {
        String acctLabel = getAcctLabelFromSheetName(sheetName);
        String inst = getInstitutionFromSheetName(sheetName);
        Account act = acctRepo.findByInstitutionAndAccountLabel(inst, acctLabel);
        return act.getId();
    }

    public void closeAll () {
        try {
            fis.close();
        } catch (IOException e) {
            log.error("Couldn't close");
        }
    }
}
