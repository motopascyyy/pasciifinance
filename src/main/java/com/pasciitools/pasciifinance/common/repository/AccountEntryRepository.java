package com.pasciitools.pasciifinance.common.repository;

import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.entity.SummarizedAccountEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    List<AccountEntry> getLatestResults (LocalDateTime d);

    String LATEST_ENTRIES_QUERY_FOR_ACC =
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
                    "    a.active = 'TRUE' AND" +
                    "    ae.account_id = ?";

    @Query(value= LATEST_ENTRIES_QUERY_FOR_ACC, nativeQuery = true)
    List<AccountEntry> getLatestResults (LocalDateTime d, Long accountId);

    AccountEntry findTopByOrderByEntryDateDesc();
    AccountEntry findTopByAccountEqualsOrderByIdDesc(Account account);

    AccountEntry findTopByAccountAndEntryDateLessThanEqualOrderByEntryDateDesc(Account account, LocalDateTime entryDate);

}
