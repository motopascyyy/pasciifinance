package com.pasciitools.pasciifinance.common.service;

import com.pasciitools.pasciifinance.common.entity.Security;
import com.pasciitools.pasciifinance.common.repository.SecurityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SecurityService {
    @Autowired
    private SecurityRepository secRepo;

    private Map<String, Security> secByTickerCache;

    public Security getSecurity (String ticker) {
        if (secByTickerCache == null)
            secByTickerCache = new ConcurrentHashMap<>();
        Security result = secByTickerCache.get(ticker);
        if (result == null) {
            var possibleResult = secRepo.findById(ticker);
            if (possibleResult.isPresent()) {
                result = possibleResult.get();
                secByTickerCache.put(ticker, result);
            }
        }
        return result;
    }
}
