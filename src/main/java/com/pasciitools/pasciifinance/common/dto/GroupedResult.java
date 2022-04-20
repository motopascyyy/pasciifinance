package com.pasciitools.pasciifinance.common.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface GroupedResult {
    public LocalDate getEntryDate();
    public BigDecimal getBookValue();
    public BigDecimal getMarketValue();
}
