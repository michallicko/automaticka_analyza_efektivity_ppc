package cz.sortivo.reporting.model;

import java.util.List;

import cz.sortivo.ppcgenerator.model.Account;

public class AccountStatObject {
    Account account;
    List<Stat> stats;
    
    public Account getAccount() {
        return account;
    }
    
    public void setAccount(Account account) {
        this.account = account;
    }
    
    public List<Stat> getStats() {
        return stats;
    }
    
    public void setStats(List<Stat> stats) {
        this.stats = stats;
    }
    
    
    
}
