package com.pasciitools.pasciifinance.common.service;

import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AccountService {
    @Autowired
    private AccountRepository acctRepo;

    private Map<String, Account> accountByInstitutionCache;

    public Account getAccount(String institutionName, String accountName) {
        String key = institutionName + "_" + accountName;
        if (accountByInstitutionCache == null)
            accountByInstitutionCache = new ConcurrentHashMap<>();
        Account result = accountByInstitutionCache.get(key);
        if (result == null) {
            result = acctRepo.findByInstitutionAndAccountLabel(institutionName, accountName);
            accountByInstitutionCache.put(key, result);
        }
        return result;
    }

    public Account getAccountFromSheetName(String sheetName) {
        String acctLabel = getAcctLabelFromSheetName(sheetName);
        String inst = getInstitutionFromSheetName(sheetName);
        return getAccount(inst, acctLabel);
    }

    public String getInstitutionFromSheetName (String sheetName) {
        return sheetName.split(" ")[0].trim();
    }

    public String getAcctLabelFromSheetName (String sheetName) {
        String inst = getInstitutionFromSheetName(sheetName);
        return sheetName.substring((inst.length())).trim();
    }

    public int getTotalNumberOfAccounts() {
        return (int) acctRepo.count();
    }

    public Account getAccountFromAccountNumber(String text) {
        return acctRepo.findByInstitutionAccountId(text);
    }

    public AccountRepository getAcctRepo() {
        return acctRepo;
    }
}
