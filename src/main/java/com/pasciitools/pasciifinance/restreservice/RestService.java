package com.pasciitools.pasciifinance.restreservice;

import com.pasciitools.pasciifinance.account.Account;
import com.pasciitools.pasciifinance.account.AccountEntry;
import com.pasciitools.pasciifinance.account.AccountEntryRepository;
import com.pasciitools.pasciifinance.account.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.*;

@RestController
public class RestService {
    private static final Logger log = LoggerFactory.getLogger(RestService.class);
    private final NumberFormat nfCAD = NumberFormat.getCurrencyInstance(Locale.CANADA);

    @Autowired
    private AccountEntryRepository entryRepo;

    @Autowired
    private AccountRepository accountRepo;

    @GetMapping("/currentValue")
    public String getCurrentValue() throws Exception{
        String result = "$0";
        List<AccountEntry> latestEntries = entryRepo.getLatestResults(new Date());
        Date latestMaxDate = latestEntries.get(0).getEntryDate();
        java.util.Calendar cal = GregorianCalendar.getInstance();
        cal.setTime( latestMaxDate );
        cal.add(GregorianCalendar.DAY_OF_YEAR, -1);
        List<AccountEntry> previousEntries = entryRepo.getLatestResults(cal.getTime());
        List<Long> accountIds = new ArrayList<>();


        BigDecimal previousBalance = new BigDecimal(0);
        for (AccountEntry entry : previousEntries) {
            if (entry.getAccount().isJointAccount()){
                previousBalance = previousBalance.add(entry.getMarketValue().divide(BigDecimal.valueOf(2)));
            } else {
                previousBalance = previousBalance.add(entry.getMarketValue());
            }
        }

        BigDecimal currentBalance = new BigDecimal(0);
        for (AccountEntry entry : latestEntries) {
            if (entry.getAccount().isJointAccount()){
                currentBalance = currentBalance.add(entry.getMarketValue().divide(BigDecimal.valueOf(2)));
            } else {
                currentBalance = currentBalance.add(entry.getMarketValue());
            }
        }

        if (currentBalance.doubleValue() > previousBalance.doubleValue()) {
            result = currentBalance.doubleValue() != 0.0 ? String.format("%s up from %s", getFormattedAsCurrency(currentBalance), getFormattedAsCurrency(previousBalance)) : result;
        }
        else
            result = currentBalance.doubleValue() != 0.0 ? String.format("%s down from %s", getFormattedAsCurrency(currentBalance), getFormattedAsCurrency(previousBalance)) : result;
        return result;
    }

    private String getFormattedAsCurrency(BigDecimal dec) {
        dec = dec.setScale(2, RoundingMode.HALF_UP);
        return nfCAD.format(dec.doubleValue());
    }

    @GetMapping("/accounts")
    public List<Account> getAccounts (@RequestParam boolean isActive) {
        return accountRepo.findAllByActive(isActive);
    }

    @GetMapping("/allAccounts")
    public List<Account> getAccounts () {
        Iterable<Account> iterable = accountRepo.findAll();
        Iterator<Account> iter = iterable.iterator();
        List<Account> accounts = new ArrayList<>();
        while (iter.hasNext()) {
            accounts.add(iter.next());
        }
        return accounts;
    }

    @GetMapping("/latest")
    public AccountEntry getMaxEntry () {
        try {
            AccountEntry latest = entryRepo.findTopByOrderByEntryDateDesc();
            return latest;
        } catch (Exception e) {
            log.error("Couldn't find latest entry due to: " + e.getMessage(), e);
            return null;
        }
    }

    @PostMapping("/entry")
    public AccountEntry newEntry (@RequestBody AccountEntry entry) {
        return entryRepo.save(entry);
    }

    @PostMapping("/entries")
    public List<AccountEntry> newEntries (@RequestBody List<AccountEntry> entries) {
        Iterable iterable = entryRepo.saveAll(entries);
        Iterator iter = iterable.iterator();
        List<AccountEntry> entriesSaved = new ArrayList<>();
        while (iter.hasNext()) {
            entriesSaved.add((AccountEntry) iter.next());
        }
        if (entriesSaved.size() != entries.size()) {
            log.error("Not all entries submitted were saved. Please investigate:\n\t Submitted:\n" + entries + "\n\nSaved:\n" + entriesSaved);
        } else {
            log.debug("All entries submitted were saved.");
        }

        return entriesSaved;
    }
}
