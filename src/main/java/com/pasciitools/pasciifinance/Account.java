package com.pasciitools.pasciifinance;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Account {

    @Id
    @GeneratedValue
    private Long id;
    private String accountType; //RRSP, Taxable, TFSA...
    private String institution; //WealthSimple, TD...
    private String institutionAccountId; //
    private String accountLabel;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    private boolean isActive;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getInstitutionAccountId() {
        return institutionAccountId;
    }

    public void setInstitutionAccountId(String institutionAccountId) {
        this.institutionAccountId = institutionAccountId;
    }

    public String getAccountLabel() {
        return accountLabel;
    }

    public void setAccountLabel(String accountLabel) {
        this.accountLabel = accountLabel;
    }

    public String toString () {
        return String.format("%s %s", institution, accountLabel);
    }


}
