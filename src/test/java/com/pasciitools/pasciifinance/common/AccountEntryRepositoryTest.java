package com.pasciitools.pasciifinance.common;

import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.repository.AccountEntryRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AccountEntryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountEntryRepository entryRepository;


    @Test
    void injectedComponentsAreNotNull(){

        assertThat(entityManager).isNotNull();
        assertThat(entryRepository).isNotNull();
    }

    @Test
    public void testFetching () {

        Account acc = new Account();
        acc.setAccountLabel("Test Account 123");
        acc.setAccountType("RRSP");
        acc.setJointAccount(false);
        acc.setInstitutionAccountId(acc.getAccountLabel());
        acc.setInstitution("TD");

        AccountEntry entry = new AccountEntry();
        entry.setAccount(acc);
        entry.setMarketValue(555);
        entry.setBookValue(444);
        entityManager.persist(acc);
        entityManager.persist(entry);
        assertThat(entryRepository.count() == 1);
        Iterable<AccountEntry> results = entryRepository.findAll();

        results.forEach((en) -> {
            assertThat(en.getAccount().equals(acc));
            assertThat(en.getMarketValue().intValue() == 555);
            assertThat(en.getBookValue().intValue() == 444);
        });
    }
}
