package com.pasciitools.pasciifinance.common.entity;

import java.time.LocalDate;

public interface SummarizedAccountEntry {

    public LocalDate getEntryDate();

    public Double getBookValue();

    public Double getMarketValue();

}
