package com.pasciitools.pasciifinance.common.repository;

import com.pasciitools.pasciifinance.common.entity.Security;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecurityRepository extends CrudRepository<Security, String> {
    Optional<Security> findById(String ID);
}
