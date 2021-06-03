package com.pasciitools.pasciifinance.common.repository;

import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.entity.SummarizedAccountEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*
 * Note, this annotation isn't strictly necessary for the app to work, but IntelliJ doesn't detect a successful autowiring
 * Configuration class in the `com.pasciitools.pasciifinance.batch` module.
 */
@Repository
public interface AccountEntryRepository extends CrudRepository<AccountEntry, Long> {

    String LATEST_ENTRIES_QUERY =
            "select " +
            "    * " +
            "from " +
            "    account_entry ae " +
            "    inner join account a on ae.account_id = a.id " +
            "    inner join ( " +
            "        select " +
            "            max(entry_date) as maxDate, " +
            "            ae.account_id as aid " +
            "        from " +
            "            account_entry ae " +
            "        where " +
            "            ae.entry_date <= ? " +
            "        group by " +
            "            ae.account_id " +
            "    ) max_ae on ae.account_id = max_ae.aid " +
            "    and ae.entry_date = maxDate " +
            "where " +
            "    a.active = 'TRUE' " +
            "order by " +
            "    ae.account_id ";

    @Query(value= LATEST_ENTRIES_QUERY, nativeQuery = true)
    List<AccountEntry> getLatestResults (LocalDate d);

    AccountEntry findTopByOrderByEntryDateDesc();


    String TIME_SERIES_ENTRY_QUERY =
            "select " +
            "   e_date as ENTRY_DATE, " +
            "   sum(coalesce(BOOK_VALUE, PREVIOUS_BOOK_VALUE)) as BOOK_VALUE, " +
            "   sum(coalesce(MARKET_VALUE, PREVIOUS_MARKET_VALUE)) as MARKET_VALUE " +
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
            "               distinct to_char(entry_date, 'yyyy-mm-dd') as e_date, " +
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
            "               LATEST_ACCOUNT_ENTRY lae " +
            "               inner join account_entry ae on lae.ENTRY_ID = ae.id " +
            "               inner join account acc on ae.account_id = acc.id " +
            "         ) entries on dates.e_date = entries.e_date " +
            "         and dates.acc_id = entries.account_id " +
            "   ) " +
            "group by " +
            "   e_date " +
            "order by " +
            "   e_date asc ";

    @Query(value= TIME_SERIES_ENTRY_QUERY, nativeQuery = true)
    List<SummarizedAccountEntry> findAccountEntriesByEntryDateAfter(LocalDate startDate);

}
