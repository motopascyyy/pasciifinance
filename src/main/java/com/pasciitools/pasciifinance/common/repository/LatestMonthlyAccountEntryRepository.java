package com.pasciitools.pasciifinance.common.repository;

import com.pasciitools.pasciifinance.common.dto.SummedByDateAccountEntries;
import com.pasciitools.pasciifinance.common.entity.LatestMonthlyAccountEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LatestMonthlyAccountEntryRepository extends ReadOnlyRepository<LatestMonthlyAccountEntry, Long>{
    List<LatestMonthlyAccountEntry> findAllByAccountId(Long accountId);

    @Query(value= SUMMED_AS_GROUP, nativeQuery = true)
    List<SummedByDateAccountEntries> findAccountEntriesByEntryDateAfter(LocalDate startDate);

    List<LatestMonthlyAccountEntry> findSummarizedAccountEntriesByAccountId (Long accountId);

    String SUMMED_AS_GROUP =
            "select " +
            "   entry_date as entryDate, " +
            "   sum(book_value) as bookValue, " +
            "   sum(market_value) as marketValue " +
            "from LATEST_MONTHLY_ACCOUNT_ENTRY_VIEW " +
            "group by " +
            "   entryDate " +
            "order by " +
            "   entryDate";

    @Query(value = SUMMED_AS_GROUP, nativeQuery = true)
    List<SummedByDateAccountEntries> findSummarizedValues();

    List<LatestMonthlyAccountEntry> findAllByEntryDateBefore(LocalDate date);
}
