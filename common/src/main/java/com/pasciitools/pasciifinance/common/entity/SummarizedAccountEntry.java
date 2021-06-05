package com.pasciitools.pasciifinance.common.entity;

import java.time.LocalDate;

public interface SummarizedAccountEntry {

    public String getEntryDate();

    public Double getBookValue();

    public Double getMarketValue();

}
