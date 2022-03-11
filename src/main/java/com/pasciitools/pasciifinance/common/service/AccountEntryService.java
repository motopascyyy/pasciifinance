package com.pasciitools.pasciifinance.common.service;

import com.pasciitools.pasciifinance.common.repository.AccountEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountEntryService {
    @Autowired
    private AccountEntryRepository entryRepo;

    public int getTotalNumberOfEntries() {
        return (int) entryRepo.count();
    }

    public AccountEntryRepository getEntryRepo () {
        return entryRepo;
    }

}
