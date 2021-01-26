package com.pasciitools.pasciifinance;

import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccountRepository extends CrudRepository<Account, Long> {
    Account findById(long id);
    Account findByInstitutionAndAccountLabel(String institution, String accountLabel);
}
