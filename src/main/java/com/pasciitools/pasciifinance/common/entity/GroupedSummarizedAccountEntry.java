package com.pasciitools.pasciifinance.common.entity;

import java.math.BigDecimal;

public interface GroupedSummarizedAccountEntry {
    public String getEntryDate();
    public BigDecimal getBookValue();
    public BigDecimal getMarketValue();
}
