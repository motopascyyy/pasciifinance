package com.pasciitools.pasciifinance.common.dto;

import com.pasciitools.pasciifinance.common.entity.AccountEntry;
import com.pasciitools.pasciifinance.common.exception.NumberOutOfRangeException;

import java.math.BigDecimal;

public class EntryAllocation {
    private Long accountId;
    private BigDecimal canadianEqtPct;
    private BigDecimal usEqtPct;
    private BigDecimal internationalEqtPct;
    private BigDecimal emergingMktsEqtPct;
    private BigDecimal cadFixedIncomePct;
    private BigDecimal globalFixedIncomePct;
    private BigDecimal cashPct;
    private BigDecimal otherPct;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
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

    public AccountEntry updateEntry (AccountEntry entry) throws NumberOutOfRangeException{
        entry.setCanadianEqtPct(returnAppropriatePct(entry.getCanadianEqtPct(), canadianEqtPct));
        entry.setUsEqtPct(returnAppropriatePct(entry.getUsEqtPct(),usEqtPct));
        entry.setInternationalEqtPct(returnAppropriatePct(entry.getInternationalEqtPct(),internationalEqtPct));
        entry.setEmergingMktsEqtPct(returnAppropriatePct(entry.getEmergingMktsEqtPct(),emergingMktsEqtPct));
        entry.setCadFixedIncomePct(returnAppropriatePct(entry.getCadFixedIncomePct(), cadFixedIncomePct));
        entry.setGlobalFixedIncomePct(returnAppropriatePct(entry.getGlobalFixedIncomePct(), globalFixedIncomePct));
        entry.setCashPct(returnAppropriatePct(entry.getCashPct(),cashPct));
        entry.setOtherPct(returnAppropriatePct(entry.getOtherPct(), otherPct));
        return entry;
    }

    private BigDecimal returnAppropriatePct (BigDecimal oldVal, BigDecimal newVal) throws NumberOutOfRangeException {
        if (newVal != null && (newVal.doubleValue() < 0 || newVal.doubleValue() > 1))
            throw new NumberOutOfRangeException(String.format("Value provided (%s) needs to be between 0 and 1 as it is a percentage", newVal.toString()));
        if (oldVal == null && newVal == null) {
            return BigDecimal.ZERO;
        } else if (oldVal != null && newVal == null) {
            return oldVal;
        } else
            return newVal;
    }
}


