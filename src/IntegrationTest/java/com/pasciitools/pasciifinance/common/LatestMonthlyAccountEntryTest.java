package com.pasciitools.pasciifinance.common;

import com.pasciitools.pasciifinance.common.dto.SummedByDateAccountEntries;
import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.entity.LatestMonthlyAccountEntry;
import com.pasciitools.pasciifinance.common.repository.AccountEntryRepository;
import com.pasciitools.pasciifinance.common.repository.AccountRepository;
import com.pasciitools.pasciifinance.common.repository.LatestMonthlyAccountEntryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@DataJpaTest
@ActiveProfiles("qa-file")
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
public class LatestMonthlyAccountEntryTest {

    private static final Logger log = LoggerFactory.getLogger(LatestMonthlyAccountEntryTest.class);

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LatestMonthlyAccountEntryRepository sumRepository;

    @Autowired
    private AccountEntryRepository entryRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private boolean dateChecker (LocalDate date, int year, int month) {
        return date != null &&
                date.getYear() == year &&
                date.getMonthValue() == month;
    }

    private int findIndexInList(LocalDate date, List<LatestMonthlyAccountEntry> list) {
        int result = -1;
        for (int i = 0; i < list.size(); i++){
            var listEntry = list.get(i);
            if (listEntry.getEntryDate().equals(date)){
                result = i;
                break;
            }
        }
        return result;
    }

    @Test
    public void reconcileSummaryToRaw () {
        var rawResults = entryRepository.findAll();

        /**
         * 0. Find a list of all active accounts.
         * 1. find the earliest date in the account_entry table for active accounts
         * 2. for each year-month combo, from the date found in step 1, to today, for each account, find the latest possible entry for that account
         * 3. store each of the latest results found in a map where key = year-month string, value = List<AccountEntry>. Each list should be the exact same length
         */

        //0
        var allAccounts = accountRepository.findAll();

        //1
        String query = "select min(entry_date) from account_entry";
        var earliestDate = jdbcTemplate.queryForObject(query, LocalDateTime.class);
        var earliestMonth = earliestDate.getMonth();
        var earliestYear = earliestDate.getYear();

        var now = LocalDateTime.now();

        //2
        Map<String, List<AccountEntry>> expectedMap = new HashMap<>();
        var nextDate = earliestDate;
        while (nextDate.isBefore(now)) {
            var eom = YearMonth.from(nextDate).atEndOfMonth().atStartOfDay().plusDays(1).minusSeconds(1);
            String key = eom.getYear() + "-" + eom.getMonthValue();
            List<AccountEntry> list = new ArrayList<>();
            for (Account a : allAccounts) {
                var entry = entryRepository.findTopByAccountAndEntryDateLessThanEqualOrderByEntryDateDesc(a, eom);
                if (entry == null) {
                    log.debug(String.format("Unable to find results for account %s (%s) with EOM %s. Creating dummy entry.", a, a.getId(), eom));
                    entry = new AccountEntry();
                    entry.setAccount(a);
                    entry.setBookValue(0);
                    entry.setMarketValue(0);
                    entry.setEntryDate(eom);
                }
                list.add(entry);
            }
            expectedMap.put(key, list);
            nextDate = YearMonth.from(nextDate).atDay(1).plusMonths(1).atStartOfDay();
        }

        var actualList = sumRepository.findAllByEntryDateBefore(now.toLocalDate());
        for (LatestMonthlyAccountEntry latestMonthlyAccountEntry : actualList) {
            var expectedEntry = findMatchingInMap(expectedMap, latestMonthlyAccountEntry);
            Assertions.assertNotNull(expectedEntry, String.format("Null entry found trying to find a matching entry in the expected map for Account: %s and Entry ID: %s and Entry Date: %s", latestMonthlyAccountEntry.getAccountId(), latestMonthlyAccountEntry.getEntryId(), latestMonthlyAccountEntry.getEntryDate()));

            if (expectedEntry.getAccount().isJointAccount()) {
                Assertions.assertEquals(expectedEntry.getBookValue().divide(BigDecimal.valueOf(2), RoundingMode.HALF_UP).doubleValue(), latestMonthlyAccountEntry.getBookValue().doubleValue(), String.format("Differences found for Account ID %s, Entry Date %s.", latestMonthlyAccountEntry.getAccountId(), latestMonthlyAccountEntry.getEntryDate()));
                Assertions.assertEquals(expectedEntry.getMarketValue().divide(BigDecimal.valueOf(2)).doubleValue(), latestMonthlyAccountEntry.getMarketValue().doubleValue(), String.format("Differences found for Account ID %s, Entry Date %s.", latestMonthlyAccountEntry.getAccountId(), latestMonthlyAccountEntry.getEntryDate()));
            } else {
                Assertions.assertEquals(expectedEntry.getBookValue().doubleValue(), latestMonthlyAccountEntry.getBookValue().doubleValue(), String.format("Differences found for Account ID %s, Entry Date %s.", latestMonthlyAccountEntry.getAccountId(), latestMonthlyAccountEntry.getEntryDate()));
                Assertions.assertEquals(expectedEntry.getMarketValue().doubleValue(), latestMonthlyAccountEntry.getMarketValue().doubleValue(), String.format("Differences found for Account ID %s, Entry Date %s.", latestMonthlyAccountEntry.getAccountId(), latestMonthlyAccountEntry.getEntryDate()));
            }
        }

    }

    private int getTotalCountFromMap (Map<String, List<AccountEntry>> entries) {
        var count = 0;
        for (String key : entries.keySet()) {
            count += entries.get(key).size();
        }
        return count;
    }

    private AccountEntry findMatchingInMap (Map<String, List<AccountEntry>> entries, LatestMonthlyAccountEntry latestMonthlyAccountEntry) {
        LocalDate entryDateKey = latestMonthlyAccountEntry.getEntryDate();
        String key = entryDateKey.getYear() + "-" + entryDateKey.getMonthValue();
        var entryList= entries.get(key);
        for (AccountEntry entry : entryList) {
            if (latestMonthlyAccountEntry.getAccountId().equals(entry.getAccount().getId()))
                return entry;
        }
        return null;
    }

    @Test
    public void testGroupedSummaries () {
        var summarizedValues = sumRepository.findSummarizedValues();
        var accountCount = accountRepository.count();
        Assertions.assertNotNull(summarizedValues);

        Map<String, List<AccountEntry>> monthlyResults = getMapOfMonthlyEntries();


        for (SummedByDateAccountEntries summedByDateAccountEntries : summarizedValues) {
            String key = summedByDateAccountEntries.getEntryDate().getYear() + "-" + summedByDateAccountEntries.getEntryDate().getMonthValue();
            List<AccountEntry> currentMonthResults = monthlyResults.get(key);
            var bookValue = BigDecimal.ZERO;
            var marketValue = BigDecimal.ZERO;
            for (AccountEntry entry : currentMonthResults) {
                /**
                 * Check if the entry we're dealing with is actually from the current month.
                 * We know that entries that aren't in this month won't be captured in the view logic.
                 * In that situation, records need to be rolled-forward automatically.
                 */
                if (entry.getEntryDate().getYear() == summedByDateAccountEntries.getEntryDate().getYear() &&
                        entry.getEntryDate().getMonthValue() == summedByDateAccountEntries.getEntryDate().getMonthValue()) {
                    if (entry.getAccount().isJointAccount()) {
                        bookValue = bookValue.add(entry.getBookValue().divide(BigDecimal.valueOf(2)));
                        marketValue = marketValue.add(entry.getMarketValue().divide(BigDecimal.valueOf(2)));
                    } else {
                        bookValue = bookValue.add(entry.getBookValue());
                        marketValue = marketValue.add(entry.getMarketValue());
                    }
                }
            }
            Assertions.assertEquals(bookValue.doubleValue(), summedByDateAccountEntries.getBookValue().doubleValue(), String.format("Could not match the book value on date %s.\n%s", summedByDateAccountEntries.getEntryDate(), getListOfBookValues(currentMonthResults)));
            Assertions.assertEquals(marketValue.doubleValue(), summedByDateAccountEntries.getMarketValue().doubleValue(), String.format("Could not match the market value on date %s. \n%s", summedByDateAccountEntries.getEntryDate(), getListOfMarketValues(currentMonthResults)));
        }
    }

    private String getListOfBookValues (List<AccountEntry> entries) {
        String result = "";
        for (AccountEntry entry : entries) {
            if (entry.getAccount().isJointAccount())
                result += String.format("\tAccount: %s - Entry Date: %s - Book Value: %s\n", entry.getAccount().getId(), entry.getEntryDate(), entry.getBookValue().divide(BigDecimal.valueOf(2)));
            else
                result += String.format("\tAccount: %s - Entry Date: %s - Book Value: %s\n", entry.getAccount().getId(), entry.getEntryDate(), entry.getBookValue());
        }

        return result;
    }

    private String getListOfMarketValues (List<AccountEntry> entries) {
        String result = "";
        for (AccountEntry entry : entries) {
            result += String.format("\tAccount: %s - Entry Date: %s - Market Value: %s\n", entry.getAccount().getId(), entry.getEntryDate(), entry.getBookValue());
        }

        return result;
    }


    private Map<String, List<AccountEntry>> getMapOfMonthlyEntries () {
        var allAccounts = accountRepository.findAll();
        Map<String, List<AccountEntry>> expectedMap = new HashMap<>();
        //1
        String query = "select min(entry_date) from account_entry";
        var earliestDate = jdbcTemplate.queryForObject(query, LocalDateTime.class);
        var earliestMonth = earliestDate.getMonth();
        var earliestYear = earliestDate.getYear();

        var now = LocalDateTime.now();

        //2
        var nextDate = earliestDate;
        while (nextDate.isBefore(now)) {
            var eom = YearMonth.from(nextDate).atEndOfMonth().atStartOfDay().plusDays(1).minusSeconds(1);
            String key = eom.getYear() + "-" + eom.getMonthValue();
            List<AccountEntry> list = new ArrayList<>();
            for (Account a : allAccounts) {
                var entry = entryRepository.findTopByAccountAndEntryDateLessThanEqualOrderByEntryDateDesc(a, eom);
                if (entry == null) {
                    log.debug(String.format("Unable to find results for account %s (%s) with EOM %s. Creating dummy entry.", a, a.getId(), eom));
                    entry = new AccountEntry();
                    entry.setAccount(a);
                    entry.setBookValue(0);
                    entry.setMarketValue(0);
                    entry.setEntryDate(eom);
                }
                list.add(entry);
            }
            expectedMap.put(key, list);
            nextDate = YearMonth.from(nextDate).atDay(1).plusMonths(1).atStartOfDay();
        }
        return expectedMap;
    }
}
