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

    private BigDecimal canadianEqtPct;
    private BigDecimal usEqtPct;
    private BigDecimal internationalEqtPct;
    private BigDecimal emergingMktsEqtPct;
    private BigDecimal cadFixedIncomePct;
    private BigDecimal globalFixedIncomePct;
    private BigDecimal cashPct;
    private BigDecimal otherPct;


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

    public BigDecimal getCanadianEqtPct() {
        return canadianEqtPct;
    }

    public void setCanadianEqtPct(BigDecimal canadianEqtPct) {
        this.canadianEqtPct = canadianEqtPct;
    }

    public BigDecimal getUsEqtPct() {
        return usEqtPct;
    }

    public void setUsEqtPct(BigDecimal usEqtPct) {
        this.usEqtPct = usEqtPct;
    }

    public BigDecimal getInternationalEqtPct() {
        return internationalEqtPct;
    }

    public void setInternationalEqtPct(BigDecimal internationalEqtPct) {
        this.internationalEqtPct = internationalEqtPct;
    }

    public BigDecimal getEmergingMktsEqtPct() {
        return emergingMktsEqtPct;
    }

    public void setEmergingMktsEqtPct(BigDecimal emergingMktsEqtPct) {
        this.emergingMktsEqtPct = emergingMktsEqtPct;
    }

    public BigDecimal getCadFixedIncomePct() {
        return cadFixedIncomePct;
    }

    public void setCadFixedIncomePct(BigDecimal cadFixedIncomePct) {
        this.cadFixedIncomePct = cadFixedIncomePct;
    }

    public BigDecimal getGlobalFixedIncomePct() {
        return globalFixedIncomePct;
    }

    public void setGlobalFixedIncomePct(BigDecimal globalFixedIncomePct) {
        this.globalFixedIncomePct = globalFixedIncomePct;
    }

    public BigDecimal getCashPct() {
        return cashPct;
    }

    public void setCashPct(BigDecimal cashPct) {
        this.cashPct = cashPct;
    }

    public BigDecimal getOtherPct() {
        return otherPct;
    }

    public void setOtherPct(BigDecimal otherPct) {
        this.otherPct = otherPct;
    }
}
