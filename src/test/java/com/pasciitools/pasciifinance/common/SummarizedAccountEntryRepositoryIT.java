package com.pasciitools.pasciifinance.common;

import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.entity.SummarizedAccountEntry;
import com.pasciitools.pasciifinance.common.repository.AccountEntryRepository;
import com.pasciitools.pasciifinance.common.repository.AccountRepository;
import com.pasciitools.pasciifinance.common.repository.SummarizedAccountEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("qa-file")
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
public class SummarizedAccountEntryRepositoryIT {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SummarizedAccountEntryRepository sumRepository;

    @Autowired
    private AccountEntryRepository entryRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void reconcileResults () {

        List<Account> accounts = new ArrayList<>();
        var sumRepoResults = sumRepository.findAll();
        var entryRepoResults = sumRepository.findAccountEntriesByEntryDateAfter(LocalDate.now());
        assertEquals(sumRepoResults.size(), entryRepoResults.size());
        List<SummarizedAccountEntry> expectedResults = new ArrayList<>();
        for (int i = 0; i < sumRepoResults.size(); i++) {
            SummarizedAccountEntry viewEntry = sumRepoResults.get(i);
            SummarizedAccountEntry queriedEntry = entryRepoResults.get(i);
            if (dateChecker(queriedEntry.getEntryDate(), 2018, 02) ||
                    dateChecker(queriedEntry.getEntryDate(),2018,04) ||
                    dateChecker(queriedEntry.getEntryDate(),2021,11))
                System.out.println (queriedEntry.getEntryDate() + "\t" + queriedEntry.getBookValue() + "\t" + queriedEntry.getMarketValue());
            assertEquals(queriedEntry, viewEntry);
            if (queriedEntry != null) {
                int index = findIndexInList(queriedEntry.getEntryDate(), expectedResults);
                if (index == -1) {
                    var groupedEntry = new SummarizedAccountEntry(queriedEntry.getEntryDate(), queriedEntry.getBookValue(), queriedEntry.getMarketValue());
                    expectedResults.add(groupedEntry);
                } else {
                    var groupedEntry = expectedResults.get(index);
                    var replacementEntry = new SummarizedAccountEntry(
                            groupedEntry.getEntryDate(),
                            groupedEntry.getBookValue().add(queriedEntry.getBookValue()),
                            groupedEntry.getMarketValue().add(queriedEntry.getMarketValue()));
                    expectedResults.set(index, replacementEntry);
                }
            }
        }


        var groupedResults = sumRepository.findSummarizedValues();
        assertEquals(expectedResults.size(), groupedResults.size());
        for (int i = 0; i < expectedResults.size(); i++) {
            var expected = expectedResults.get(i);
            var grouped = groupedResults.get(i);
            assertEquals(expected.getEntryDate(), grouped.getEntryDate());
            if (!expected.getBookValue().equals(grouped.getBookValue()) || !expected.getMarketValue().equals(grouped.getMarketValue()))
                System.out.println (grouped.getEntryDate() + "\t" + expected.getBookValue() + "\t" + grouped.getBookValue() + "\t" + expected.getMarketValue() + "\t" + grouped.getMarketValue());
//            assertEquals(expected.getBookValue(), grouped.getBookValue(), String.format("Mismatch for book value on date: %s (expected) and %s (actual)",expected.getEntryDate(), grouped.getEntryDate()));
//            assertEquals(expected.getMarketValue(), grouped.getMarketValue(), String.format("Mismatch for book value on date: %s (expected) and %s (actual)",expected.getEntryDate(), grouped.getEntryDate()));
        }
    }

    private boolean dateChecker (LocalDate date, int year, int month) {
        if (date != null && date.getYear() == year && date.getMonthValue() == month)
            return true;
        return false;
    }

    private int findIndexInList(LocalDate date, List<SummarizedAccountEntry> list) {
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
        var activeAccounts = accountRepository.findAllByActive(true);

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
            System.out.println (String.format("Parsing for date: %s", nextDate));
            var eom = YearMonth.from(nextDate).atEndOfMonth().atStartOfDay();
            String key = eom.getYear() + "-" + eom.getMonthValue();
            List<AccountEntry> list = new ArrayList<>();
            for (Account a : activeAccounts) {
                var entry = entryRepository.findTopByAccountAndEntryDateLessThanEqualOrderByEntryDateDesc(a, eom);
                list.add(entry);
            }
            expectedMap.put(key, list);
            nextDate = YearMonth.from(nextDate).atDay(1).plusMonths(1).atStartOfDay();
            System.out.println(String.format("Next date to study will be: %s", nextDate));
        }
        System.out.println (expectedMap.keySet());

        var actualList = sumRepository.findAccountEntriesByEntryDateAfter(now.toLocalDate());

    }

    private int getTotalCountFromMap (Map<String, List<AccountEntry>> entries) {
        var count = 0;
        var iter = entries.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
//            for (AccountEntry entry : entries.get(key)){
//                if (entry != null && entry.getAccount().isActive())
//                    count ++;
//            }
            count+= entries.get(key).size();
        }
        return count;
    }
}
