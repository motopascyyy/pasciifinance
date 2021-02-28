package com.pasciitools.pasciifinance.account;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(uniqueConstraints={@UniqueConstraint(columnNames={"institution", "accountLabel"})})
public class Account {

    @Id
    @GeneratedValue
    private Long id;
    private String accountType; //RRSP, Taxable, TFSA...
    private String institution; //WealthSimple, TD...
    private String institutionAccountId; //
    private String accountLabel;
    private boolean jointAccount;
    private boolean active;

    @OneToMany(mappedBy = "account")
    private List<AccountEntry> accountEntries;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


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

    public boolean isJointAccount() {
        return jointAccount;
    }

    public void setJointAccount(boolean jointAccount) {
        this.jointAccount = jointAccount;
    }
}
