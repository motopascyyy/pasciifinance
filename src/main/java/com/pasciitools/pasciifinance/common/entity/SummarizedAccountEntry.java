package com.pasciitools.pasciifinance.common.entity;

import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Immutable
@Table(name = "SUMMARIZED_ACCOUNT_ENTRY_BY_MONTH")
public class SummarizedAccountEntry implements Comparable<SummarizedAccountEntry>{

    @Id
    private Long entryId;

    @Column(name = "acc_id")
    private Long accId;

    private String entryDate;
    private BigDecimal bookValue;
    private BigDecimal marketValue;

    public Long getAccId() {
        return accId;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public BigDecimal getBookValue() {
        return bookValue;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public SummarizedAccountEntry () {};

    @Override
    public int compareTo(SummarizedAccountEntry o) {
        if (this.equals(o))
            return 0;
        else
            return this.getEntryDate().compareTo(o.getEntryDate());
    }

    @Override
    public boolean equals (Object o) {

        if (o instanceof SummarizedAccountEntry s) {
            if (!getEntryDate().equals(s.getEntryDate()))
                return false;
            if (!getAccId().equals(s.getAccId()))
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
