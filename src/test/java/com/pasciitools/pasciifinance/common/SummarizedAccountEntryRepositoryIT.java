package com.pasciitools.pasciifinance.common;

import com.jayway.jsonpath.internal.function.numeric.Sum;
import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.entity.SummarizedAccountEntry;
import com.pasciitools.pasciifinance.common.repository.AccountEntryRepository;
import com.pasciitools.pasciifinance.common.repository.AccountRepository;
import com.pasciitools.pasciifinance.common.repository.SummarizedAccountEntryRepository;
import com.pasciitools.pasciifinance.common.service.AccountEntryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

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

    @Test
    public void reconcileResults () {

        List<Account> accounts = new ArrayList<>();
        var sumRepoResults = sumRepository.findAll();
        var entryRepoResults = sumRepository.findAccountEntriesByEntryDateAfter(LocalDate.now());
        assertEquals(sumRepoResults.size(), entryRepoResults.size());
        for (int i = 0; i < sumRepoResults.size(); i++) {
            SummarizedAccountEntry viewEntry = sumRepoResults.get(i);
            SummarizedAccountEntry queriedEntry = entryRepoResults.get(i);

            assertNotNull(queriedEntry, "Queried Entry at position: " + i + " was null.");
            assertNotNull(viewEntry, "View Entry at position: " + i + " was null.");
            assertTrue(viewEntry.getBookValue() != null);
            assertTrue(viewEntry.getMarketValue() != null);
            assertTrue(viewEntry.getEntryDate() != null);
            assertTrue(viewEntry.getAccId() != null);
            assertEquals(queriedEntry, viewEntry);
        }
    }

}
