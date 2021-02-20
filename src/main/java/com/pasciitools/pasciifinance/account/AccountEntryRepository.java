package com.pasciitools.pasciifinance.account;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface AccountEntryRepository extends CrudRepository<AccountEntry, Long> {

//    String latestEntries = "select ae.ID as ID, ae.ACCOUNT_ID as ACCOUNT_ID, ae.BOOK_VALUE as BOOK_VALUE, ae.ENTRY_DATE as ENTRY_DATE, ae.MARKET_VALUE as MARKET_VALUE from account_entry ae inner join account a on ae.account_id = a.id inner join (select max(entry_date) as maxDate, ae.account_id as aid from account_entry ae where ae.entry_date <= ? group by ae.account_id) max_ae on ae.account_id = max_ae.aid and ae.entry_date = maxDate where a.is_active = 'TRUE'";
//    String latestEntries = "select ae.*, a.* from account_entry ae inner join account a on ae.account_id = a.id inner join (select max(entry_date) as maxDate, ae.account_id as aid from account_entry ae where ae.entry_date <= ? group by ae.account_id) max_ae on ae.account_id = max_ae.aid and ae.entry_date = maxDate where a.is_active = 'TRUE'";
    String latestEntries = "select * from account_entry ae inner join account a on ae.account_id = a.id inner join (select max(entry_date) as maxDate, ae.account_id as aid from account_entry ae where ae.entry_date <= ? group by ae.account_id) max_ae on ae.account_id = max_ae.aid and ae.entry_date = maxDate where a.active = 'TRUE'";

    @Query(value= latestEntries, nativeQuery = true)
    List<AccountEntry> getLatestResults (Date d);

    AccountEntry findTopByOrderByEntryDateDesc();

}
