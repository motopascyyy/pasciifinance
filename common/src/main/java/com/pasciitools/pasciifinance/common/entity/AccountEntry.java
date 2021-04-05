package com.pasciitools.pasciifinance.common.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"entryDate", "ACCOUNT_ID"})})
public class AccountEntry {

    @Id
    @GeneratedValue
    private Long id;
    private Date entryDate;

    private BigDecimal bookValue;

    private BigDecimal marketValue;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

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

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(double marketValue) {
        this.marketValue = BigDecimal.valueOf(marketValue);
    }
    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }

    public String toString () {
        String accountID = getAccount() == null ? "TBD" : getAccount().getId().toString();
        String entryD = getEntryDate() == null ? "TBD" : getEntryDate().toString();
        String bookV = bookValue == null ? "TBD" : String.valueOf(getBookValue());
        String marketV = marketValue == null ? "TBD" : String.valueOf(getMarketValue());
        return String.format("Account ID: %s, Entry Date: %s, Book Value: %s, Market Value: %s", accountID, entryD, bookV, marketV);
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
