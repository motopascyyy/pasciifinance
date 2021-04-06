package com.pasciitools.pasciifinance.batch.seeding;

import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.repository.AccountRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.time.DateTimeException;
import java.util.Date;

@EnableJpaRepositories(basePackages="com.pasciitools.pasciifinance")
@EntityScan(basePackages = "com.pasciitools.pasciifinance")
public class AccountEntryItemProcessor implements ItemProcessor<Row, AccountEntry> {

    private static Logger log = LoggerFactory.getLogger(AccountEntryItemProcessor.class);

    @Autowired
    private AccountRepository acctRepo;

    @Override
    public AccountEntry process(final Row excelRow) throws Exception {
        String sheetName = excelRow.getSheet().getSheetName();
        AccountEntry ae = new AccountEntry();
        ae.setAccount(getAcctId(sheetName));
        Cell dateCell = excelRow.getCell(0);
        Cell bookValueCell = excelRow.getCell(1);
        Cell marketValueCell = excelRow.getCell(2);
        if (dateCell == null || dateCell.getDateCellValue() == null) {
            String message  = String.format("Exiting sheet %s at %o. The row had no value for the date field", sheetName, excelRow.getRowNum());
            if (log.isDebugEnabled())
                log.debug(message);
            throw new DateTimeException(message);
        }
        Date entryDate = dateCell.getDateCellValue();
        double bookValue = bookValueCell != null ? bookValueCell.getNumericCellValue() : marketValueCell.getNumericCellValue();
        double marketValue = marketValueCell.getNumericCellValue();
        ae.setEntryDate(entryDate);
        ae.setBookValue(bookValue);
        ae.setMarketValue(marketValue);
        return ae;
    }

    /**
     * TODO This is inefficient right now, and ripe for caching optimizations.
     * The number of accounts is so small that it's of minimal concern at this time.
     * @param sheetName
     * @return
     */
    private Account getAcctId (String sheetName) {
        String acctLabel = getAcctLabelFromSheetName(sheetName);
        String inst = getInstitutionFromSheetName(sheetName);
        return acctRepo.findByInstitutionAndAccountLabel(inst, acctLabel);
    }

    private String getInstitutionFromSheetName (String sheetName) {
        return sheetName.split(" ")[0].trim();
    }

    private String getAcctLabelFromSheetName (String sheetName) {
        String inst = getInstitutionFromSheetName(sheetName);
        return sheetName.substring((inst.length())).trim();
    }


}
