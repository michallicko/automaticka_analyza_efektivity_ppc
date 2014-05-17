package cz.sortivo.reporting.model;

import cz.sortivo.ppcgenerator.model.Account;
import cz.sortivo.ppcgenerator.model.User;
import cz.sortivo.sklikapi.ForeignAccount;

public class SKlikForeignAccount extends ForeignAccount implements Account {

    private static final long serialVersionUID = 1L;

    public SKlikForeignAccount(ForeignAccount a) {
        super(a.getUserId(), a.getAccess(), a.getRelationStatus(), a.getRelationName(), a.getUsername());
    }

    @Override
    public User getUser() {
        return null;
    }

    @Override
    public String getUserName() {
        return getUsername();
    }

    @Override
    public Number getAccountId() {
        return getUserId();
    }

}
