package com.pasciitools.pasciifinance.rest.restreservice;

import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.entity.AccountAllocationEntry;
import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.repository.AccountAllocationEntryRepository;
import com.pasciitools.pasciifinance.common.repository.AccountEntryRepository;
import com.pasciitools.pasciifinance.common.repository.AccountRepository;
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

    @Autowired
    private AccountAllocationEntryRepository allocationRepo;

    @GetMapping("/currentValue")
    public String getCurrentValue() {
        String result = "$0";
        List<AccountEntry> latestEntries = entryRepo.getLatestResults(new Date());
        if (latestEntries != null && latestEntries.size() > 0) {
            Date latestMaxDate = latestEntries.get(0).getEntryDate();
            Calendar cal = Calendar.getInstance();
            cal.setTime(latestMaxDate);
            cal.add(Calendar.DAY_OF_YEAR, -1);
            List<AccountEntry> previousEntries = entryRepo.getLatestResults(cal.getTime());
            List<Long> accountIds = new ArrayList<>();


            BigDecimal previousBalance = new BigDecimal(0);
            for (AccountEntry entry : previousEntries) {
                if (entry.getAccount().isJointAccount()) {
                    previousBalance = previousBalance.add(entry.getMarketValue().divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP));
                } else {
                    previousBalance = previousBalance.add(entry.getMarketValue());
                }
            }

            BigDecimal currentBalance = new BigDecimal(0);
            for (AccountEntry entry : latestEntries) {
                if (entry.getAccount().isJointAccount()) {
                    currentBalance = currentBalance.add(entry.getMarketValue().divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP));
                } else {
                    currentBalance = currentBalance.add(entry.getMarketValue());
                }
            }

            if (currentBalance.doubleValue() > previousBalance.doubleValue()) {
                result = currentBalance.doubleValue() != 0.0 ? String.format("%s up from %s", getFormattedAsCurrency(currentBalance), getFormattedAsCurrency(previousBalance)) : result;
            } else
                result = currentBalance.doubleValue() != 0.0 ? String.format("%s down from %s", getFormattedAsCurrency(currentBalance), getFormattedAsCurrency(previousBalance)) : result;
            return result;
        } else {
            return "No results retrieved. Apparently you're broke!";
        }
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
        Iterable<AccountEntry> iterable = entryRepo.saveAll(entries);
        Iterator<AccountEntry> iter = iterable.iterator();
        List<AccountEntry> entriesSaved = new ArrayList<>();
        while (iter.hasNext()) {
            entriesSaved.add((AccountEntry) iter.next());
        }
        if (entriesSaved.size() != entries.size()) {
            log.error("Not all entries submitted were saved. Please investigate:\n\t Submitted:\n" + entries + "\n\nSaved:\n" + entriesSaved);
        } else {
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

        BigDecimal cadEqtValue = new BigDecimal(0);
        BigDecimal usEqtValue = new BigDecimal(0);
        BigDecimal intEqtyValue = new BigDecimal(0);

        BigDecimal fixedIncomeValue = new BigDecimal(0);

        BigDecimal cashValue = new BigDecimal(0);
        BigDecimal otherAssetsValue = new BigDecimal(0);

        var now = new Date();

        //Step 1
        List<AccountEntry> latestTotals = entryRepo.getLatestResults(now);

        //Step 2
        List<AccountAllocationEntry> latestAllocPcts = allocationRepo.getLatestResults(now);

        //Step 3
        for (AccountEntry total : latestTotals) {
            Long accId = total.getAccount().getId();
            AccountAllocationEntry pctMatch = null;
            for (AccountAllocationEntry allocEntry : latestAllocPcts) {
                if (allocEntry.getAccount().getId().equals(accId)) {
                    pctMatch = allocEntry;
                    break;
                }
            }
            //Step 3.1 + 3.2
            cadEqtValue = cadEqtValue.add(total.getMarketValue().multiply(pctMatch.getCanadianEquity()));
            usEqtValue = usEqtValue.add(total.getMarketValue().multiply(pctMatch.getUsEquity()));
            intEqtyValue = intEqtyValue.add(total.getMarketValue().multiply(pctMatch.getInternationalEquity()));

            //For FI, sum both CAD and Global values. For my use case that's as granular as I want to compute
            fixedIncomeValue = fixedIncomeValue.add(total.getMarketValue().multiply(pctMatch.getCanadianFixedIncome()).add(total.getMarketValue().multiply(pctMatch.getGlobalFixedIncome())));

            cashValue = cashValue.add(total.getMarketValue().multiply(pctMatch.getCashEquivalent()));
            otherAssetsValue = otherAssetsValue.add(total.getMarketValue().multiply(pctMatch.getOtherAssets())); //for my use case, this should always be 0
        }

        //Step 4 //make this a unit test

        //Step 5
        BigDecimal currentBalance = new BigDecimal(0);
        for (AccountEntry entry : latestTotals) {
            if (entry.getAccount().isJointAccount()) {
                currentBalance = currentBalance.add(entry.getMarketValue().divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP));
            } else {
                currentBalance = currentBalance.add(entry.getMarketValue());
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
