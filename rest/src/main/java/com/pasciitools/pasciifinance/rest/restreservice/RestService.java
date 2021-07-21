package com.pasciitools.pasciifinance.rest.restreservice;

import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.entity.SummarizedAccountEntry;
import com.pasciitools.pasciifinance.common.repository.AccountEntryRepository;
import com.pasciitools.pasciifinance.common.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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
    public String getCurrentValue() {
        var result = "$0";
        var now = LocalDateTime.now();
        var latestEntries = entryRepo.getLatestResults(now);
        if (latestEntries != null && !latestEntries.isEmpty()) {
            var previousEntries = entryRepo.getLatestResults(now.minus(1, ChronoUnit.DAYS));
            var previousBalance = getBalanceFromListOfEntries(previousEntries);
            var currentBalance = getBalanceFromListOfEntries(latestEntries);

            if (currentBalance.doubleValue() > previousBalance.doubleValue()) {
                result = currentBalance.doubleValue() != 0.0 ? String.format("%s up from %s", getFormattedAsCurrency(currentBalance), getFormattedAsCurrency(previousBalance)) : result;
            } else if (currentBalance.doubleValue() < previousBalance.doubleValue())
                result = currentBalance.doubleValue() != 0.0 ? String.format("%s down from %s", getFormattedAsCurrency(currentBalance), getFormattedAsCurrency(previousBalance)) : result;
            else
                result = String.format("No change week over week. Current balance: %s", getFormattedAsCurrency(currentBalance));
            return result;
        } else {
            return "No results retrieved. Apparently you're broke!";
        }
    }

    @GetMapping("/latestEntries")
    public List<AccountEntry> getLatestEntries () {
        return entryRepo.getLatestResults(LocalDateTime.now());
    }

    private BigDecimal getBalanceFromListOfEntries (List<AccountEntry> entries) {
        var balance = new BigDecimal(0);
        if (entries != null) {
            for (AccountEntry entry : entries) {
                if (entry.getAccount().isJointAccount()) {
                    balance = balance.add(entry.getMarketValue().divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP));
                } else {
                    balance = balance.add(entry.getMarketValue());
                }
            }
        }
        return balance;
    }


    @GetMapping("/time_series_summary")
    public List<SummarizedAccountEntry> getTimeSeriesSummary(@RequestParam
                                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDate startDate) {
        return entryRepo.findAccountEntriesByEntryDateAfter(startDate);
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
            return entryRepo.findTopByOrderByEntryDateDesc();
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
        //TODO: change the way I process dates. Right now, it's not interpreting the TZ so I need an override hack like below
        LocalDateTime now = LocalDateTime.now();
        for (AccountEntry entry : entries) {
            entry.setEntryDate(now);
        }
        Iterable<AccountEntry> iterable = entryRepo.saveAll(entries);
        Iterator<AccountEntry> iter = iterable.iterator();
        List<AccountEntry> entriesSaved = new ArrayList<>();
        while (iter.hasNext()) {
            entriesSaved.add(iter.next());
        }
        if (entriesSaved.size() != entries.size()) {
            log.error("Not all entries submitted were saved. Please investigate:%n%t Submitted:%n" + entries + "%n%nSaved:%n" + entriesSaved);
        } else {
            if (log.isDebugEnabled())
                log.debug(String.format("All %s entries submitted were saved.", entriesSaved.size()));
        }

        return entriesSaved;
    }

    @GetMapping("/total_allocation")
    public Map<String, Double> totalAllocation () {
        Map<String, Double> totalAllocationMap = new HashMap<>();
        /*
        1. Get latest totals per active account
        2. Get latest allocation percentages per account
        3. For each account
            1. Compute $ amounts for each bucket in account
            2. Add value computed to appropriate bucket in master map
        4. Verify total summed up of percentages matches total assets (more of a unit test than a PROD test)
        5. Compute percentages of each bucket in relation to total
        6. Compute percentages of investment buckets (i.e not cash or cash equiv)
         */

        var cadEqtValue = new BigDecimal(0);
        var usEqtValue = new BigDecimal(0);
        var intEqtyValue = new BigDecimal(0);

        var fixedIncomeValue = new BigDecimal(0);

        var cashValue = new BigDecimal(0);
        var otherAssetsValue = new BigDecimal(0);

        var now = LocalDateTime.now();

        //Step 1
        List<AccountEntry> latestTotals = entryRepo.getLatestResults(now);

        //Step 3
        for (AccountEntry total : latestTotals) {

            if (total.getCanadianEqtPct() != null) {
                //Step 3.1 + 3.2
                var marketVal = total.getMarketValue();
                var localCadEqty = marketVal.multiply(total.getCanadianEqtPct());
                cadEqtValue = cadEqtValue.add(localCadEqty);
                usEqtValue = usEqtValue.add(marketVal.multiply(total.getUsEqtPct()));
                intEqtyValue = intEqtyValue.add(marketVal.multiply(total.getInternationalEqtPct()));

                //For FI, sum both CAD and Global values. For my use case that's as granular as I want to compute
                fixedIncomeValue = fixedIncomeValue.add(total.getMarketValue().multiply(total.getCadFixedIncomePct())
                        .add(total.getMarketValue().multiply(total.getGlobalFixedIncomePct())));

                cashValue = cashValue.add(total.getMarketValue().multiply(total.getCashPct()));
                otherAssetsValue = otherAssetsValue.add(total.getMarketValue().multiply(total.getOtherPct())); //for my use case, this should always be 0
            } else {
                log.warn(String.format("Skipping total allocation percentage calc for account %s - %s because there were no asset allocations specified", total.getAccount().getInstitution(), total.getAccount().getAccountLabel()));
            }
        }

        //Step 4 //make this a unit test

        //Step 5
        var currentBalance = new BigDecimal(0);
        for (AccountEntry entry : latestTotals) {
            if (entry.getCanadianEqtPct() != null) {
                if (entry.getAccount().isJointAccount()) {
                    currentBalance = currentBalance.add(entry.getMarketValue().divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP));
                } else {
                    currentBalance = currentBalance.add(entry.getMarketValue());
                }
            } else {
                log.warn(String.format("Skipping total balance calc for account %s - %s because there were no asset allocations specified", entry.getAccount().getInstitution(), entry.getAccount().getAccountLabel()));
            }
        }

        //Step 6
        totalAllocationMap.put("Canadian Equity", cadEqtValue.divide(currentBalance, RoundingMode.HALF_UP).doubleValue());
        totalAllocationMap.put("US Equity", usEqtValue.divide(currentBalance, RoundingMode.HALF_UP).doubleValue());
        totalAllocationMap.put("International Equity", intEqtyValue.divide(currentBalance, RoundingMode.HALF_UP).doubleValue());
        totalAllocationMap.put("Fixed Income", fixedIncomeValue.divide(currentBalance, RoundingMode.HALF_UP).doubleValue());
        totalAllocationMap.put("Cash and Equivalents", cashValue.divide(currentBalance, RoundingMode.HALF_UP).doubleValue());
        totalAllocationMap.put("Other Assets", otherAssetsValue.divide(currentBalance, RoundingMode.HALF_UP).doubleValue());

        return totalAllocationMap;
    }
}
