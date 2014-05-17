package cz.sortivo.reporting.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cz.sortivo.ppcgenerator.exception.ForbiddenAccessException;
import cz.sortivo.ppcgenerator.model.Account;
import cz.sortivo.ppcgenerator.model.Provider;
import cz.sortivo.ppcgenerator.model.SklikAccount;
import cz.sortivo.ppcgenerator.model.User;
import cz.sortivo.ppcgenerator.service.SklikAccountService;
import cz.sortivo.reporting.model.SKlikForeignAccount;
import cz.sortivo.sklikapi.Client;
import cz.sortivo.sklikapi.ForeignAccount;
import cz.sortivo.sklikapi.exception.InvalidRequestException;
import cz.sortivo.sklikapi.exception.SKlikException;
import cz.sortivo.utils.NumberUtils;

@Service
public class ReportingAccountService {
    
   Logger logger = LoggerFactory.getLogger(ReportingAccountService.class);

    @Autowired
    private SklikAccountService sklikAccountService;
    
    /**
     * Obtain user accounts for specified provider.
     * @param user - PPCHit user
     * @param provider 
     * @return collection of found accounts
     */
    public Collection<Account> getAccounts(User user, Provider provider){
        switch(provider){
        case SKLIK:
            return getSklikAccounts(user);
        case ADWORDS:
            return null;
        default:
            throw new UnsupportedOperationException("Stat operation for specified provider not supported yet!");
        }
    }
    
    
    /**
     * Get SKlik low-level accounts list for specified user
     * @param user
     * @return account list
     */
    public Collection<Account> getSklikAccounts(User user){
          
        Collection<Account> sklikAccounts = new HashSet<>();
        List<SklikAccount> accounts = sklikAccountService.getUserSklikAccounts(user);
        
        
        Client client;
        for(SklikAccount a : accounts){
            try {
                client = new Client();
                client.login(a.getLogin(), a.getPassword());
                for(ForeignAccount fa : client.getForeignActiveAccounts(true)){
                    sklikAccounts.add(new SKlikForeignAccount(fa));
                }
            } catch (InvalidRequestException | SKlikException e) {
                logger.error("Specifed sklik account is not valid!", e);
            } 
        }
        
       return sklikAccounts;
    }
    
    
    public Account getUserAccountByIdAndProvider(Number accountId, User user, Provider provider){
        Collection<Account> accounts = getAccounts(user, provider);
        
        Iterator<Account> accountsIterator = accounts.iterator();
        Account account;
        while(accountsIterator.hasNext()){
            account = accountsIterator.next();
            if(account.getAccountId().longValue() == accountId.longValue()){
                return account;
            }
        }
        throw new ForbiddenAccessException();
    }
}
