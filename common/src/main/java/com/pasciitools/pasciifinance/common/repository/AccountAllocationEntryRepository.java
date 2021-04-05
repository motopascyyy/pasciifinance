package com.pasciitools.pasciifinance.common.repository;

import com.pasciitools.pasciifinance.common.entity.AccountAllocationEntry;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface AccountAllocationEntryRepository extends CrudRepository<AccountAllocationEntry, Long> {
    final String LATEST_ENTRIES_QUERY = "select * from account_allocation_entry ae inner join account a on ae.account_id = a.id inner join (select max(entry_date) as maxDate, ae.account_id as aid from account_allocation_entry ae where ae.entry_date <= ? group by ae.account_id) max_ae on ae.account_id = max_ae.aid and ae.entry_date = maxDate where a.active = 'TRUE' order by ae.account_id";

    @Query(value= LATEST_ENTRIES_QUERY, nativeQuery = true)
    List<AccountAllocationEntry> getLatestResults (Date d);
}