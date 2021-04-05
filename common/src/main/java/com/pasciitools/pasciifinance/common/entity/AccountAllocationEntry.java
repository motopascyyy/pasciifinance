package com.pasciitools.pasciifinance.common.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
public class AccountAllocationEntry {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name = "ACCOUNT_ID")
    private Account account;

    private Date entryDate;
    private BigDecimal canadianEquity;
    private BigDecimal usEquity;
    private BigDecimal cashEquivalent;
    private BigDecimal internationalEquity;
    private BigDecimal globalFixedIncome;
    private BigDecimal canadianFixedIncome;
    private BigDecimal otherAssets;

    public Date getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(Date entryDate) {
        this.entryDate = entryDate;
    }

    public BigDecimal getCanadianEquity() {
        return canadianEquity;
    }

    public void setCanadianEquity(BigDecimal canadianEquity) {
        this.canadianEquity = canadianEquity;
    }

    public BigDecimal getUsEquity() {
        return usEquity;
    }

    public void setUsEquity(BigDecimal usEquity) {
        this.usEquity = usEquity;
    }

    public BigDecimal getCashEquivalent() {
        return cashEquivalent;
    }

    public void setCashEquivalent(BigDecimal cashEquivalent) {
        this.cashEquivalent = cashEquivalent;
    }

    public BigDecimal getInternationalEquity() {
        return internationalEquity;
    }

    public void setInternationalEquity(BigDecimal internationalEquity) {
        this.internationalEquity = internationalEquity;
    }

    public BigDecimal getGlobalFixedIncome() {
        return globalFixedIncome;
    }

    public void setGlobalFixedIncome(BigDecimal globalFixedIncome) {
        this.globalFixedIncome = globalFixedIncome;
    }

    public BigDecimal getCanadianFixedIncome() {
        return canadianFixedIncome;
    }

    public void setCanadianFixedIncome(BigDecimal canadianFixedIncome) {
        this.canadianFixedIncome = canadianFixedIncome;
    }

    public BigDecimal getOtherAssets() {
        return otherAssets;
    }

    public void setOtherAssets(BigDecimal otherAssets) {
        this.otherAssets = otherAssets;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
