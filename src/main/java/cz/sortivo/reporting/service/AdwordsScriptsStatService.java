package cz.sortivo.reporting.service;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import cz.sortivo.ppcgenerator.model.Account;
import cz.sortivo.reporting.model.EntityType;
import cz.sortivo.reporting.model.Stat;
import cz.sortivo.reporting.model.StatFormat;
import cz.sortivo.reporting.model.StatFormatAdwordsScriptsSingleReport;

@Service
public class AdwordsScriptsStatService implements StatCreator {

    private static final Logger logger = LoggerFactory.getLogger(AdwordsScriptsStatService.class);

    @SuppressWarnings({ "unchecked", "unused" })
    @Override
    public List<Stat> createStats(StatFormat statsFormat, Account account) {  
        List<StatFormatAdwordsScriptsSingleReport> adwordsStatsFormat = (List<StatFormatAdwordsScriptsSingleReport>) statsFormat.getData();
        
        List<Stat> stats = new LinkedList<>();
        Stat stat;
        for (StatFormatAdwordsScriptsSingleReport singleReport : adwordsStatsFormat){
            stat = getStatObject(singleReport);
            stat.setProvider(StatService.PROVIDER_ADWORDS);
            stat.setUser(account.getUser());
            stat.setEntityType(EntityType.getInstance(singleReport.getEntityTypeName().toUpperCase()));
            stats.add(stat);
        }
        return stats;
    }
    
    private static Stat getStatObject(StatFormatAdwordsScriptsSingleReport singleReport){
        Field singleReportFields[] = StatFormatAdwordsScriptsSingleReport.class.getDeclaredFields();
        
        Stat stat = new Stat();
        for (Field field : singleReportFields){
            try {
                field.setAccessible(true);
                StatService.setProperty(stat, field.getName(), field.get(singleReport));
            } catch (IllegalArgumentException | IllegalAccessException e) {
                logger.debug("Could not set property " + field.getName() + " of Stat object", e);
            }
        }
        return stat;
    } 

}
