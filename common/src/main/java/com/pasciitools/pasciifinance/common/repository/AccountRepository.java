package com.pasciitools.pasciifinance.common.repository;

import com.pasciitools.pasciifinance.common.entity.Account;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AccountRepository extends CrudRepository<Account, Long> {
    Account findById(long id);
    Account findByInstitutionAndAccountLabel(String institution, String accountLabel);
    List<Account> findAllByActive(boolean isActive);
    Account findByInstitutionAccountId(String id);
}
