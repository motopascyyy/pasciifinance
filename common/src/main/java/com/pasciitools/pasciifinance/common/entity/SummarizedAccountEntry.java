package com.pasciitools.pasciifinance.common.entity;

import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@Immutable
public class SummarizedAccountEntry {

    @Id
    private long entryId;

    @Column(name = "acc_id")
    private Long accId;

    private String entryDate;
    private Double bookValue;
    private Double marketValue;

    public Long getAccId() {
        return accId;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public Double getBookValue() {
        return bookValue;
    }

    public Double getMarketValue() {
        return marketValue;
    }


}
