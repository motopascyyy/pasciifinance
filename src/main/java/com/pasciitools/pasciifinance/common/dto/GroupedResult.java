package com.pasciitools.pasciifinance.common.dto;

import java.math.BigDecimal;

public interface GroupedResult {
    public String getEntryDate();
    public BigDecimal getBookValue();
    public BigDecimal getMarketValue();
}
