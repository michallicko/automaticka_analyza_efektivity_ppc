package cz.sortivo.reporting.service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.hibernate.PropertyValueException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import cz.sortivo.ppcgenerator.model.Account;
import cz.sortivo.ppcgenerator.model.SklikAccount;
import cz.sortivo.ppcgenerator.service.AccountAuthService;
import cz.sortivo.ppcgenerator.service.SklikAccountService;
import cz.sortivo.reporting.exception.StatsCouldNotBeObtainedException;
import cz.sortivo.reporting.model.EntityType;
import cz.sortivo.reporting.model.SKlikForeignAccount;
import cz.sortivo.reporting.model.Stat;
import cz.sortivo.reporting.model.StatFormat;
import cz.sortivo.reporting.model.StatFormatSklikImpl;
import cz.sortivo.sklikapi.Ad;
import cz.sortivo.sklikapi.AdDAO;
import cz.sortivo.sklikapi.Campaign;
import cz.sortivo.sklikapi.CampaignDAO;
import cz.sortivo.sklikapi.Client;
import cz.sortivo.sklikapi.ForeignAccount;
import cz.sortivo.sklikapi.Group;
import cz.sortivo.sklikapi.GroupDAO;
import cz.sortivo.sklikapi.Keyword;
import cz.sortivo.sklikapi.KeywordDAO;
import cz.sortivo.sklikapi.SKlikObject;
import cz.sortivo.sklikapi.Stats;
import cz.sortivo.sklikapi.StatsDAO;
import cz.sortivo.sklikapi.exception.InvalidRequestException;
import cz.sortivo.sklikapi.exception.SKlikException;
import cz.sortivo.utils.StringUtils;

@Service
public class SklikStatService implements StatCreator{
    
    private static final Logger logger = LoggerFactory.getLogger(SklikStatService.class);
    private static final int MAX_STATS_DEPTH = 10;
    
    private static final Map<String, String> PROPERTY_NAME_MAP;
    static{
        Map<String, String> propertyNameMap = new HashMap<>();

        propertyNameMap.put("name","name");
        //propertyNameMap.put("cpc","avgCpc");
        propertyNameMap.put("avgPosition","avgPosition");
        propertyNameMap.put("clicks","clicks");
        propertyNameMap.put("conversions","conversions");
        propertyNameMap.put("money","cost");
        propertyNameMap.put("ctr","ctr");
        propertyNameMap.put("impressions","impressions");
        propertyNameMap.put("dayBudget","budget");
      
        propertyNameMap.put("id","entityId");
       
        PROPERTY_NAME_MAP = Collections.unmodifiableMap(propertyNameMap);
    }
    
    @Autowired
    private SklikAccountService accountService;
    
    @Autowired
    private AccountAuthService accountAuthService;
    
    @Autowired
    private StatService reportingService;
    
    
    
    
    private Map<String, Object> getFieldsToSet(Object o ){
        Field[] fields = o.getClass().getDeclaredFields();
        Map<String, Object> res = new TreeMap<>();
        for (Field f : fields){
            if (PROPERTY_NAME_MAP.containsKey(f.getName())){
                res.put(f.getName(), o);
            }
        }
        return res;
    }
    
    /**
     * Calculate missing fields if other useful data are presented in object
     * @param stat
     */
    private void calculateMissingFields(Stat stat){
        if ((stat.getCtr() == null || stat.getCtr().equals(0D)) && !stat.getImpressions().equals(0L)){
            stat.setCtr(stat.getClicks()/stat.getImpressions().doubleValue());
        }
        
        if ((stat.getConversionRate() == null || stat.getConversionRate().equals(0D)) && !stat.getClicks().equals(0L)){
            stat.setConversionRate(stat.getConversions()/stat.getClicks().doubleValue());
        }
        
        if ((stat.getAvgCpc() == null || stat.getAvgCpc().equals(0D)) && !stat.getClicks().equals(0L)){
            stat.setAvgCpc(stat.getCost()/stat.getClicks().doubleValue());
        }
        
    }
    
    @Override
    public List<Stat> createStats(StatFormat statsFormat, Account account) {
        StatFormatSklikImpl sklikStats = (StatFormatSklikImpl) statsFormat.getData();
        Stat superStat = new Stat();
        SKlikForeignAccount foreignAccount = (SKlikForeignAccount) account;
        superStat.setSklikUserId(foreignAccount.getUserId());
        List<Stat> stats = new LinkedList<>();
        superStat.setRefDate(sklikStats.getRefDate());
        superStat.setProvider(StatService.PROVIDER_SKLIK);
        superStat.setUser(account.getUser());
        StatService.setProperty(superStat, "campaignId", sklikStats.getCampaignId());
        StatService.setProperty(superStat, "adGroupId", sklikStats.getAdGroupId());
        
        
        
        
        SKlikObject superObj = sklikStats.getEntity();
        if (superObj instanceof Campaign)superStat.setEntityType(EntityType.CAMPAIGN);
        if (superObj instanceof Group)superStat.setEntityType(EntityType.ADGROUP);
        if (superObj instanceof Ad)superStat.setEntityType(EntityType.AD);
        if (superObj instanceof Keyword)superStat.setEntityType(EntityType.KEYWORD);
         
        Map<String, Object> fieldsToSet = new TreeMap<>();
        fieldsToSet.putAll(getFieldsToSet(superObj));
        fieldsToSet.putAll(getFieldsToSet(sklikStats.getStats()));
  
        
        for (String name : fieldsToSet.keySet()){
            StatService.setProperty(superStat, PROPERTY_NAME_MAP.get(name), getProperty(fieldsToSet.get(name), name));
        }
        calculateMissingFields(superStat);
        stats.add(superStat);
        for(StatFormatSklikImpl s : sklikStats.getChildren()){
                stats.addAll(createStats(s, account));
        }
       
        return stats;
    }
    
    /**
     * Gets value of specified property
     * @param o 
     * @param propertyName - name of object's property
     * @return property value
     * @throws PropertyValueException
     */
    public Object getProperty(Object o, String propertyName){
        
        String getterName = StringUtils.propertyToGetterName(propertyName);
        try {
            Method m = o.getClass().getMethod(getterName, new Class[]{});
            return m.invoke(o, (Object[])null);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new PropertyValueException("Could not get property", o.getClass().toString(), propertyName);
        }
    }
    
    @Scheduled(cron="1 1 23 * * *")
    public void updateStats(){
        List<SklikAccount> accounts = accountService.findAll();
        for(SklikAccount account : accounts){
            proceedUpdate(account);
        }
    }
    
    /**
     * Proceed statistics update for one user
     * @param account - SklikAccount with login credentials
     */
    public void proceedUpdate(SklikAccount account){
        Client client = accountAuthService.loginSKlikUser(account.getLogin(), account.getPassword());
        
        
        if (client == null){
            return; //nothing to do with bad credentials
        }
        
        //get user related accounts
        List<ForeignAccount> accounts = accountAuthService.getSKlikAccounts(client);
        
        for (ForeignAccount a : accounts){
            try {
                proceedUpdate(client, new SKlikForeignAccount(a));
            } catch (InvalidRequestException | SKlikException e) {
                logger.error("Unable to get statistics!", e);
            }
        }
    }
    
    /**
     * Proceed statistics update for sklik users related account
     * @param account - sklik account number, represents related account id
     * @param client - connected sklik client
     * @throws SKlikException 
     * @throws InvalidRequestException 
     */
    private void proceedUpdate(Client client, SKlikForeignAccount sklikAccount) throws InvalidRequestException, SKlikException{

        
        if (client == null){
            throw new IllegalArgumentException("Client mustn't be null!");
        }
        
        CampaignDAO campaignDAO = new CampaignDAO(client);
        List<Campaign> campaigns = campaignDAO.listCampaigns(sklikAccount.getUserId());
          
        for(Campaign campaign : campaigns){
            DateTime latestVersion = reportingService.getLatestVersion(sklikAccount, campaign.getId());
            if(latestVersion == null){
                latestVersion = new DateTime().minusDays(MAX_STATS_DEPTH);
            }
            try {
                proceedUpdate(client, campaign, latestVersion, new DateTime(), sklikAccount);
            } catch (StatsCouldNotBeObtainedException e) {
                logger.error("Could not update stats!", e );
                continue;
            }
        }
    }
    
    /**
     * Update statistics for campaign in specified date range.
     * @param statsDAO - created instance of statistics DAO
     * @param campaign - current campaign
     * @param startDate - starting date
     * @param endDate - final date
     * @throws SKlikException 
     * @throws InvalidRequestException 
     * @throws StatsCouldNotBeObtainedException 
     */
    private void proceedUpdate(Client client, Campaign campaign, DateTime startDate, DateTime endDate, SKlikForeignAccount account) throws InvalidRequestException, SKlikException, StatsCouldNotBeObtainedException{
        DateTime currentDate = startDate.plusDays(1).withMillisOfDay(0);
        DateTime finalDate = endDate.withMillisOfDay(0);
                
        while(currentDate.isBefore(finalDate)){   
            proceedCampaignUpdate(client, campaign, currentDate, currentDate.plusDays(1).minusMillis(1), account);
            currentDate = currentDate.plusDays(1);
        }
    }
    
    /**
     * Persists stats for specified campain to db if not exists
     * @param client
     * @param campaign
     * @param startDate
     * @param endDate
     * @throws StatsCouldNotBeObtainedException 
     */
    private void proceedCampaignUpdate(Client client, Campaign campaign, DateTime startDate, DateTime endDate, SKlikForeignAccount account ) throws StatsCouldNotBeObtainedException{
        
        StatsDAO statsDAO = new StatsDAO(client);
        
        //get campaign stats
        StatFormatSklikImpl campaignSats;
        try {
            campaignSats = new StatFormatSklikImpl(campaign, campaign.getId(), null, startDate, statsDAO.getStats(campaign, startDate, endDate));
        } catch (InvalidRequestException | SKlikException e) {
            throw new StatsCouldNotBeObtainedException("Could not obtain stats for campaign " + campaign.getName() + " [id:" + campaign.getId() + "]" , e);
        }
        
        //save campaign stats and get stat obj id
        reportingService.saveStats(this, campaignSats, account);
        
        GroupDAO groupDAO = new GroupDAO(client);
        List<Group> groups;
        try {
            groups = groupDAO.listGroups(campaign.getId());
        } catch (InvalidRequestException | SKlikException e) {
            throw new StatsCouldNotBeObtainedException("Could not list groups for " + campaign.getName() + " [id:" + campaign.getId() + "]" , e);
        }
        
        StatFormatSklikImpl stats;
        for (Group group : groups){
            stats = getGroupStats(client, statsDAO, group, startDate, endDate);
            reportingService.saveStats(this, stats, account);
        }
        
    }
      
    /**
     * Obtain statistics for group, keywords and ads through SKlikAPI
     * @param client - instance of client loaded with login credentials
     * @param statsDAO
     * @param group - group with specified id
     * @param from - stats range beginning
     * @param to - stats range end
     * @return Stats hierarchy passed to StatFormatSklikImpl
     * @throws StatsCouldNotBeObtainedException
     */
    private StatFormatSklikImpl getGroupStats(Client client, StatsDAO statsDAO, Group group, DateTime from, DateTime to) throws StatsCouldNotBeObtainedException{
        if (group == null || group.getId() == null){
            throw new IllegalArgumentException("Group or group id mustn't be null!");
        }
        
        StatFormatSklikImpl groupStatsSklikFormat;
        //get stats for specified group
        try {
            Stats groupStats = statsDAO.getStats(group, from, to);
            groupStatsSklikFormat = new StatFormatSklikImpl(group,  group.getCampaignId(), group.getId(), from, groupStats);
        } catch (InvalidRequestException | SKlikException e) {
            throw new StatsCouldNotBeObtainedException("Unable to obtain stats for group: " + group.getName() + " id: " + group.getId(), e);
        } 
        
        //get keywords stats
        try {
            groupStatsSklikFormat.getChildren().addAll(convertToSklikFormat(from, getKeywordStats(client, statsDAO, group, from, to), group.getCampaignId(), group.getId()));
        } catch (InvalidRequestException | SKlikException e) {
            logger.error("Unable to obtain statistics for keywords in group: " + group.getName() + "[id:" + group.getId() + "]", e);
        }

        //get ads stats
        try {
            groupStatsSklikFormat.getChildren().addAll(convertToSklikFormat(from, getAdStats(client, statsDAO, group, from, to), group.getCampaignId(), group.getId()));
        } catch (InvalidRequestException | SKlikException e) {
            logger.error("Unable to obtain statistics for ads in group: " + group.getName() + "[id:" + group.getId() + "]", e);
        }
        
        return groupStatsSklikFormat;
    }
    
    /**
     * Obtain statistics for all keywords in specified group.
     * @param client - instance of client loaded with login credentials
     * @param statsDAO
     * @param group - group with specified id
     * @param from - stats range beginning
     * @param to - stats range end
     * @return map of ads with corresponding stats in specified range
     * @throws InvalidRequestException
     * @throws SKlikException
     */
    @SuppressWarnings("unchecked")
    private Map<Keyword, Stats> getKeywordStats(Client client, StatsDAO statsDAO, Group group, DateTime from, DateTime to) throws InvalidRequestException, SKlikException{   
        KeywordDAO keywordDAO = new KeywordDAO(client);
        List<? extends SKlikObject> keywords = keywordDAO.listKeywords(group.getId());
        
        return (Map<Keyword, Stats>)getStatsForSklikObjects(keywords, statsDAO, from, to);
    }
    
    /**
     * Obtain statistics for all ads in specified group.
     * @param client - instance of client loaded with login credentials
     * @param statsDAO
     * @param group - group with specified id
     * @param from - stats range beginning
     * @param to - stats range end
     * @return map of ads with corresponding stats in specified range
     * @throws InvalidRequestException
     * @throws SKlikException
     */
    @SuppressWarnings("unchecked")
    private Map<Ad, Stats> getAdStats(Client client, StatsDAO statsDAO, Group group, DateTime from, DateTime to) throws InvalidRequestException, SKlikException{
        AdDAO adDAO = new AdDAO(client);
        List<? extends SKlikObject> ads = adDAO.listAds(group.getId());
        
        return (Map<Ad, Stats>)getStatsForSklikObjects(ads, statsDAO, from, to);
    }
    
    /**
     * Obtain stats for specified list of SklikObjects through SKlikAPI
     * @param objects - list of sklik objects
     * @param statsDAO
     * @param from - stats range beginning
     * @param to - stats range end
     * @return map of SKlikObjects with corresponding stats in specified range
     * @return
     * @throws InvalidRequestException
     * @throws SKlikException
     */
    private Map<? extends SKlikObject, Stats> getStatsForSklikObjects(List<? extends SKlikObject> objects, StatsDAO statsDAO, DateTime from, DateTime to) throws InvalidRequestException, SKlikException{
        Map<SKlikObject, Stats> stats = new HashMap<>();
        for (SKlikObject o : objects){
            stats.put(o, statsDAO.getStats(o, from, to));
        }
        return stats;
    }
    

    
    /**
     * Convert HashMap of sklikObject's stats to SklikFormat
     * @param stats - map of stats, FE. Map<Ad, Stats> contains stats for specified ads
     * @param groupId 
     * @param campaignId 
     * @return converted object
     */
    private List<StatFormatSklikImpl> convertToSklikFormat(DateTime refDate, Map<? extends SKlikObject, Stats> stats, Integer campaignId, Integer adGroupId){
        Set<? extends SKlikObject> keys = stats.keySet();
        
        List<StatFormatSklikImpl> statFormats = new LinkedList<>();
        for (SKlikObject s : keys){
            statFormats.add(new StatFormatSklikImpl(s, campaignId, adGroupId, refDate, stats.get(s)));
        }
        return statFormats;
    }
    
    
    
}
