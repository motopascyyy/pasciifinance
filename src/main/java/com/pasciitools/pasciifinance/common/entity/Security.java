package com.pasciitools.pasciifinance.common.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;

@Entity
public class Security {
    @Id
    @Column(name = "ticker", nullable = false)
    private String id;
    private String description;
    @Column(scale = 4, precision = 5)
    private BigDecimal canadianEqtPct;
    @Column(scale = 4, precision = 5)
    private BigDecimal usEqtPct;
    @Column(scale = 4, precision = 5)
    private BigDecimal internationalEqtPct;
    @Column(scale = 4, precision = 5)
    private BigDecimal emergingMktsEqtPct;
    @Column(scale = 4, precision = 5)
    private BigDecimal cadFixedIncomePct;
    @Column(scale = 4, precision = 5)
    private BigDecimal globalFixedIncomePct;
    @Column(scale = 4, precision = 5)
    private BigDecimal cashPct;
    @Column(scale = 4, precision = 5)
    private BigDecimal otherPct;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getId() {
        return id;
    }

    public void setId(String ticker) {
        this.id = ticker;
    }
}
