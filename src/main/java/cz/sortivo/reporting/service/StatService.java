package cz.sortivo.reporting.service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.annotations.Transactional;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cz.sortivo.ppcgenerator.exception.AdListingException;
import cz.sortivo.ppcgenerator.model.Account;
import cz.sortivo.ppcgenerator.model.AdwordsAccount;
import cz.sortivo.ppcgenerator.model.PPCCampaign;
import cz.sortivo.ppcgenerator.model.Provider;
import cz.sortivo.ppcgenerator.model.User;
import cz.sortivo.ppcgenerator.service.CampaignService;
import cz.sortivo.reporting.dao.StatFieldManager;
import cz.sortivo.reporting.dao.StatsDAO;
import cz.sortivo.reporting.dao.StatsLockDAO;
import cz.sortivo.reporting.model.AdWordsReportState;
import cz.sortivo.reporting.model.EntityType;
import cz.sortivo.reporting.model.Stat;
import cz.sortivo.reporting.model.StatFormat;
import cz.sortivo.sklikapi.Client;
import cz.sortivo.templates.AbstractService;
import cz.sortivo.utils.StringUtils;
import cz.sortivo.utils.TypeConverter;

@Service
public class StatService extends AbstractService<Stat, StatsDAO> {
    
    private static final Logger logger = LoggerFactory.getLogger(StatService.class);

    @Autowired
    private CampaignService campaignService;
    
    @Autowired
    private StatsDAO statsDAO;
    
    @Autowired
    private StatsLockDAO statsLockDAO;
    
   
 
    public static final Integer PROVIDER_SKLIK = 2;
    public static final Integer PROVIDER_ADWORDS = 1;
    
    
    public List<Object[]> getBestKeywords(DateTime from, DateTime to, Long campaignId, Long groupId, Provider provider, Integer limit){
        return statsDAO.getBestKeywordsByCostPerConversion(groupId, from, to, campaignId,  provider, limit);
    }
    

    @SuppressWarnings("unchecked")
    public List<Stat> getStats(DateTime from, DateTime to, Long campaignId, Collection<Account> accounts, Long groupId, EntityType entityType,  Provider provider, User user){
        StatFieldManager fieldManager = new StatFieldManager();
        fieldManager.loadDefaultState();
        
        //user stats - all accounts
        if (accounts != null && accounts.size() > 1 ){
            fieldManager.setIgnored("campaignId");
            fieldManager.setIgnored("entityId");
            fieldManager.setIgnored("name");
        }
               
        List<Stat> stats = statsDAO.getEntityStatsForDateRangeAndStep(groupId, from, to, entityType, fieldManager, campaignId, accounts, provider, user , null);
        return stats;
    }
    
    public List<PPCCampaign> getCampaigns(Client client){
           try {
            return campaignService.getSKlikCampaigns(client, null);
        } catch (AdListingException e) {
            return null;
        }
    }
     
    /**
     * set stat property using reflection, contains build in mechanism for 
     * solving type conflicts of Number based classes 
     * @param stat - target stat object for set operation
     * @param propertyName - String representation of property name, will be translated to setter method
     * @param propertyValue
     */
    public static void setProperty(Stat stat, String propertyName, Object propertyValue){
        
        //there is nothing to set     
//        if(stat == null && propertyValue == null || (propertyValue instanceof Number && ((Number)propertyValue).equals(0))){
//            return;
//        }

        try {
            //generate setter name using usual pattern
            String setterName = StringUtils.propertyToSetterName(propertyName);

            //try to find specified setter method on Stat object
            Field property = Stat.class.getDeclaredField(propertyName);
            Class<?> fieldClass = property.getType();
            Object setterArgument = propertyValue;
            
            if(propertyValue != null && propertyValue.getClass() != fieldClass && propertyValue instanceof Number){
              //sometimes int value is reported instead of Double in case of whole part is only presented
              //in case that user define name as number may be type wrongly interpreted as number

              setterArgument = TypeConverter.convertObject(fieldClass, (Number)propertyValue);
            }
            
            
            Method setterMethod = Stat.class.getDeclaredMethod(setterName, fieldClass);
            
            setterMethod.invoke(stat, setterArgument);
           } catch (NoSuchMethodException | SecurityException | IllegalAccessException  | InvocationTargetException | NoSuchFieldException e) {
               logger.debug("unable to use setter for setting value", e);
           } catch (IllegalArgumentException e){
               logger.debug("property value type mismatch!", e);
           } 
    }



    @Override
    protected StatsDAO getDAO() {
        return statsDAO;
    }


    public void unlock(Long campaignId, User user){
        synchronized(user.getId()+"entity"+campaignId){
            statsLockDAO.unlock(campaignId, user);
        }
    }

    /**
     * Get latest version for adwords account
     * @param account
     * @param entityId
     * @return
     */
    public AdWordsReportState getLatestVersion(AdwordsAccount account, Long entityId) {
        synchronized(account.getId()+"entity"+entityId){
            return statsDAO.getLatestGroupRefDate(account, entityId); 
        }
    }
    
    /**
     * Get latest version for sklik account
     * @param account
     * @param entityId
     * @return
     */
    public DateTime getLatestVersion(Account account, Integer entityId) {
        return statsDAO.getLatestRefDate(account , entityId, EntityType.CAMPAIGN); 
    }
    

    public Map<String, Object> getBalanceTextParameters(Long groupId, DateTime from, DateTime to, Long campaignId,
            Provider provider){
        Map<String, Object> params = new HashMap<>();
        params.put("total_keywords", statsDAO.countActiveKeywords(groupId, from, to, campaignId, provider));
        params.put("clicked_keywords", statsDAO.countClickedKeywords(groupId, from, to, campaignId, provider));
        params.put("converted_keywords", statsDAO.countConvertedKeywords(groupId, from, to, campaignId, provider));
        params.put("converted_keywords_avg_pos", statsDAO.getConvertedKeywordsAveragePos(groupId, from, to, campaignId, provider));
        params.put("non_clicked_keywords_avg_pos", statsDAO.getNonClickedKeywordsAveragePos(groupId, from, to, campaignId, provider));
        params.put("nonconverted_keywords_total_cost", statsDAO.getNonConvertedKeywordsTotalCost(groupId, from, to, campaignId, provider));
        params.put("nonconverted_keywords_total_count", statsDAO.getNonConvertedKeywordsTotalCount(groupId, from, to, campaignId, provider));
        
        return params;
        
    }

    
    @Transactional
    public void saveStats(StatCreator statCreator, StatFormat statFormat, Account account) {
        List<Stat> stats = statCreator.createStats(statFormat, account);
        this.saveAll(stats);
    }

    public String getNameForEntityId(Long entityId, EntityType entityType) {
        return statsDAO.getNameForEntityId(entityId, entityType);
    }


    public List<Object[]> getBestKeywordsByConvCount(DateTime from, DateTime to, Long campaignId, Long groupId, Provider provider, Integer limit) {
        return statsDAO.getBestKeywordsByConversions(groupId, from, to, campaignId, provider, limit);
    }


    public List<Object[]> getBestKeywordsByConvScore(DateTime from, DateTime to, Long campaignId,
            Long groupId, Provider provider, int limit) {
        return statsDAO.getKeywordsByConvScore(groupId, from, to, campaignId, provider, limit, "DESC");
    }


    public List<Object[]> getBestKeywordsByClickScore(DateTime from, DateTime to, Long campaignId,
            Long groupId, Provider provider, int limit) {
        return statsDAO.getKeywordsByClickScore(groupId, from, to, campaignId, provider, limit, "DESC");
    }


    public List<Object[]> getWorstKeywordsByClickScore(DateTime from, DateTime to, Long campaignId,
            Long groupId, Provider provider, int limit) {
        return statsDAO.getKeywordsByClickScore(groupId, from, to, campaignId, provider, limit, "ASC");
    }


    public List<Object[]> getWorstKeywordsByConvScore(DateTime from, DateTime to, Long campaignId,
            Long groupId, Provider provider, int limit) {
        return statsDAO.getKeywordsByConvScore(groupId, from, to, campaignId, provider, limit, "ASC");
    }

 
}
