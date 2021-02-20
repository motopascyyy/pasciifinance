package com.pasciitools.pasciifinance.account;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccountRepository extends CrudRepository<Account, Long> {
    Account findById(long id);
    Account findByInstitutionAndAccountLabel(String institution, String accountLabel);
    List<Account> findAllByActive(boolean isActive);
}
