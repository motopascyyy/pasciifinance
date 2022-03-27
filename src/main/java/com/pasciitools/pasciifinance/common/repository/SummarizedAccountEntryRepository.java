package com.pasciitools.pasciifinance.common.repository;

import com.pasciitools.pasciifinance.common.entity.SummarizedAccountEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SummarizedAccountEntryRepository extends ReadOnlyRepository<SummarizedAccountEntry, Long>{
    List<SummarizedAccountEntry> findAllByAccId(Long accountId);

    String TIME_SERIES_ENTRY_QUERY =
            "select " +
                    "   entry_id, " +
                    "   acc_id, " +
                    "   e_date as entry_date, " +
                    "   sum(coalesce(BOOK_VALUE, PREVIOUS_BOOK_VALUE)) as book_value, " +
                    "   sum(coalesce(MARKET_VALUE, PREVIOUS_MARKET_VALUE)) as market_value " +
                    "from " +
                    "   ( " +
                    "      select " +
                    "         dates.e_date, " +
                    "         acc_id, " +
                    "         entry_id, " +
                    "         book_value, " +
                    "         LAG(book_value) OVER ( " +
                    "            partition by dates.acc_id " +
                    "            order by " +
                    "               dates.e_date " +
                    "         ) as previous_book_value, " +
                    "         market_value, " +
                    "         LAG(market_value) OVER ( " +
                    "            partition by dates.acc_id " +
                    "            order by " +
                    "               dates.e_date " +
                    "         ) as previous_market_value " +
                    "      from " +
                    "         ( " +
                    "            select " +
                    "               distinct to_char(entry_date, 'yyyy-mm') as e_date, " +
                    "               accts.id as acc_id " +
                    "            from " +
                    "               account_entry " +
                    "               cross join ( " +
                    "                  select " +
                    "                     id " +
                    "                  from " +
                    "                     account " +
                    "               ) accts " +
                    "         ) dates " +
                    "         left join ( " +
                    "            select " +
                    "               lae.ACCOUNT_ID, " +
                    "               lae.E_DATE, " +
                    "               lae.ENTRY_ID, " +
                    "               CASE " +
                    "                  WHEN JOINT_ACCOUNT = 'TRUE' THEN BOOK_VALUE / 2 " +
                    "                  ELSE BOOK_VALUE " +
                    "               END as BOOK_VALUE, " +
                    "               CASE " +
                    "                  WHEN JOINT_ACCOUNT = 'TRUE' THEN MARKET_VALUE / 2 " +
                    "                  ELSE MARKET_VALUE " +
                    "               END as MARKET_VALUE " +
                    "            from " +
                    "               LATEST_WEEKLY_ACCOUNT_ENTRY lae " +
                    "               inner join account_entry ae on lae.ENTRY_ID = ae.id " +
                    "               inner join account acc on ae.account_id = acc.id " +
                    "         ) entries on dates.e_date = entries.e_date " +
                    "         and dates.acc_id = entries.account_id " +
                    "   ) " +
                    "group by " +
                    "   e_date, " +
                    "   acc_id, " +
                    "   entry_id " +
                    "order by " +
                    "   e_date asc";


    String TIME_SERIES_SPECIFIC_ACCOUNT_ENTRY_QUERY =
            "select " +
                    "   acc_id, " +
                    "   entry_id, " +
                    "   e_date as entry_date, " +
                    "   sum(coalesce(BOOK_VALUE, PREVIOUS_BOOK_VALUE)) as book_value, " +
                    "   sum(coalesce(MARKET_VALUE, PREVIOUS_MARKET_VALUE)) as market_value " +
                    "from " +
                    "   ( " +
                    "      select " +
                    "         dates.e_date, " +
                    "         acc_id, " +
                    "         entry_id, " +
                    "         book_value, " +
                    "         LAG(book_value) OVER ( " +
                    "            partition by dates.acc_id " +
                    "            order by " +
                    "               dates.e_date " +
                    "         ) as previous_book_value, " +
                    "         market_value, " +
                    "         LAG(market_value) OVER ( " +
                    "            partition by dates.acc_id " +
                    "            order by " +
                    "               dates.e_date " +
                    "         ) as previous_market_value " +
                    "      from " +
                    "         ( " +
                    "            select " +
                    "               distinct to_char(entry_date, 'yyyy-mm') as e_date, " +
                    "               accts.id as acc_id " +
                    "            from " +
                    "               account_entry " +
                    "               cross join ( " +
                    "                  select " +
                    "                     id " +
                    "                  from " +
                    "                     account " +
                    "               ) accts " +
                    "         ) dates " +
                    "         left join ( " +
                    "            select " +
                    "               lae.ACCOUNT_ID, " +
                    "               lae.E_DATE, " +
                    "               lae.ENTRY_ID, " +
                    "               CASE " +
                    "                  WHEN JOINT_ACCOUNT = 'TRUE' THEN BOOK_VALUE / 2 " +
                    "                  ELSE BOOK_VALUE " +
                    "               END as BOOK_VALUE, " +
                    "               CASE " +
                    "                  WHEN JOINT_ACCOUNT = 'TRUE' THEN MARKET_VALUE / 2 " +
                    "                  ELSE MARKET_VALUE " +
                    "               END as MARKET_VALUE " +
                    "            from " +
                    "               LATEST_WEEKLY_ACCOUNT_ENTRY lae " +
                    "               inner join account_entry ae on lae.ENTRY_ID = ae.id " +
                    "               inner join account acc on ae.account_id = acc.id " +
                    "         ) entries on dates.e_date = entries.e_date " +
                    "         and dates.acc_id = entries.account_id " +
                    "         where dates.acc_id = ? " +
                    "   ) " +
                    "group by " +
                    "   e_date, " +
                    "   acc_id, " +
                    "   entry_id " +
                    "order by " +
                    "   e_date asc";

    @Query(value= TIME_SERIES_ENTRY_QUERY, nativeQuery = true)
    List<SummarizedAccountEntry> findAccountEntriesByEntryDateAfter(LocalDate startDate);

    @Query(value= TIME_SERIES_SPECIFIC_ACCOUNT_ENTRY_QUERY, nativeQuery = true)
    List<SummarizedAccountEntry> findAccountEntriesForAccountByEntryDateAfter (Long accountId);
}
