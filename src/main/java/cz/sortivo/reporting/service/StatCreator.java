package cz.sortivo.reporting.service;

import java.util.List;

import cz.sortivo.ppcgenerator.model.Account;
import cz.sortivo.reporting.model.Stat;
import cz.sortivo.reporting.model.StatFormat;
import cz.sortivo.reporting.model.StatFormatAdwordsScriptsImpl;

public interface StatCreator {
    
    public List<Stat> createStats(StatFormat stats, Account account);

}
