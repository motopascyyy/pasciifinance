package com.pasciitools.pasciifinance.common.repository;

import com.pasciitools.pasciifinance.common.entity.SummarizedAccountEntry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
@EnableJpaRepositories(basePackages="com.pasciitools.pasciifinance")
@EntityScan(basePackages = "com.pasciitools.pasciifinance")
@DataJpaTest
public class SummarizedAccountEntryRepositoryIntegrationTest {

//    @Autowired
//    private SummarizedAccountEntryRepository summarizedAccountEntryRepository;

    @Test
    public void testFindAllByAccountID () {
//        List<SummarizedAccountEntry> entries = summarizedAccountEntryRepository.findAllByAccId(1L);
        List<SummarizedAccountEntry> entries = new ArrayList<>();
        System.out.println(entries.size());
        Assert.isTrue(entries != null && entries.size() > 0, "Records not able to be loaded as expected");
    }
}
