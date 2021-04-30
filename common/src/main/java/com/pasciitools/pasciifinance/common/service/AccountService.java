package com.pasciitools.pasciifinance.common.service;

import com.pasciitools.pasciifinance.common.entity.Account;
import com.pasciitools.pasciifinance.common.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
public class AccountService {
    @Autowired
    private AccountRepository acctRepo;

    public Account getAccount(String institutionName, String accountName) {
        //TODO add some caching logic here for better performance
        return acctRepo.findByInstitutionAndAccountLabel(institutionName, accountName);
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
}
