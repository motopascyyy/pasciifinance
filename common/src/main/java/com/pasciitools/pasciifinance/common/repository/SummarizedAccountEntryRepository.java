package com.pasciitools.pasciifinance.common.repository;

import com.pasciitools.pasciifinance.common.entity.SummarizedAccountEntry;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SummarizedAccountEntryRepository extends ReadOnlyRepository<SummarizedAccountEntry, Long>{
    List<SummarizedAccountEntry> findAllByAccId(Long accountId);
}
