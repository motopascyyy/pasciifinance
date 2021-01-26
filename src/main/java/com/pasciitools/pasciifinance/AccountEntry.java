package com.pasciitools.pasciifinance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.Date;

@Entity
public class AccountEntry {
    @Id
    @GeneratedValue
    private Long id;
    private Date entryDate;

    private BigDecimal bookValue;

    private BigDecimal marketValue;
    private Long accountId;

    public Date getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(Date entryDate) {
        this.entryDate = entryDate;
    }

    public double getBookValue() {
        return bookValue.doubleValue();
    }

    public void setBookValue(double bookValue) {
        this.bookValue = BigDecimal.valueOf(bookValue);
    }

    public double getMarketValue() {
        return marketValue.doubleValue();
    }

    public void setMarketValue(double marketValue) {
        this.marketValue = BigDecimal.valueOf(marketValue);
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String toString () {
        return String.format("Account ID: %s, Entry Date: %s, Book Value: %s, Market Value: %s", getAccountId(), getEntryDate(), getBookValue(), getMarketValue());
    }
}
