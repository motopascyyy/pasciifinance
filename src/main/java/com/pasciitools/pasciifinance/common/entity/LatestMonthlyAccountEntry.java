package com.pasciitools.pasciifinance.common.entity;

import org.hibernate.annotations.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Immutable
@Table(name = "LATEST_MONTHLY_ACCOUNT_ENTRY_VIEW")
public class LatestMonthlyAccountEntry implements Comparable<LatestMonthlyAccountEntry>{

    @Id
    private Long entryId;

    private Long accountId;

    private LocalDate entryDate;
    private BigDecimal bookValue;
    private BigDecimal marketValue;

    public Long getEntryId() {return entryId;};

    public Long getAccountId() {
        return accountId;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public BigDecimal getBookValue() {
        return bookValue;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public LatestMonthlyAccountEntry() {};

    public LatestMonthlyAccountEntry(LocalDate entryDate, BigDecimal bookValue, BigDecimal marketValue) {
        this.entryDate = entryDate;
        this.bookValue = bookValue;
        this.marketValue = marketValue;
        this.accountId = -1L;
        this.entryId = -1L;
    }

    @Override
    public int compareTo(LatestMonthlyAccountEntry o) {
        if (this.equals(o))
            return 0;
        else
            return this.getEntryDate().compareTo(o.getEntryDate());
    }

    @Override
    public boolean equals (Object o) {

        if (o instanceof LatestMonthlyAccountEntry s) {
            if (!getEntryDate().equals(s.getEntryDate()))
                return false;
            if (!getAccountId().equals(s.getAccountId()))
                return false;
            if (!getBookValue().equals(s.getBookValue()))
                return false;
            if (!getMarketValue().equals(s.getMarketValue()))
                return false;
            return true;
        } else {
            return false;
        }
    }
}
