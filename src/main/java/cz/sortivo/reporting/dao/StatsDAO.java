package cz.sortivo.reporting.dao;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Repository;

import cz.sortivo.ppcgenerator.model.Account;
import cz.sortivo.ppcgenerator.model.AdwordsAccount;
import cz.sortivo.ppcgenerator.model.Provider;
import cz.sortivo.ppcgenerator.model.User;
import cz.sortivo.reporting.model.AdWordsReportState;
import cz.sortivo.reporting.model.EntityType;
import cz.sortivo.reporting.model.Stat;
import cz.sortivo.reporting.model.StatSelectedField;
import cz.sortivo.reporting.service.StatService;
import cz.sortivo.templates.AbstractJpaDAO;

@Repository
public class StatsDAO extends AbstractJpaDAO<Stat> {
    
 

   Logger logger = LoggerFactory.getLogger(StatsDAO.class);

    @Override
    protected Class<Stat> getClazz() {
        return Stat.class;
    }
    
    @Autowired
    private StatsLockDAO statsLockDAO;
    
 
    
    public Stat getLatestReportByEntityIdAndEntityType(Long entityId, EntityType entityType, Provider provider){
        TypedQuery<Stat> q = em.createQuery("SELECT s FROM Stat s WHERE s.entityId = :entityId AND s.entityType = :entityType AND s.provider = :provider ORDER BY s.refDate DESC", Stat.class);
        q.setParameter("entityType", entityType);
        q.setParameter("entityId", entityId);
        q.setParameter("provider", provider.getValue());
        q.setMaxResults(1);
        
        try{
            return q.getSingleResult();
        }catch(NoResultException ex){
            return null;
        }
    }
    
   

    public AdWordsReportState getLatestGroupRefDate(AdwordsAccount account, Long campaignId) {

        //get related group latest ref date
        TypedQuery<AdWordsReportState> q = em.createQuery("SELECT new cz.sortivo.reporting.model.AdWordsReportState(s.refDate, s.entityId) FROM Stat s WHERE s.user = :user " +
        		                                    "AND s.entityType = :type AND s.provider = :provider AND s.campaignId = :campaignId" +
        		                                    " ORDER BY s.refDate DESC, s.entityId DESC", AdWordsReportState.class);
        
        q.setParameter("user", account.getUser());
        q.setParameter("type", EntityType.ADGROUP);
        q.setParameter("provider", Provider.ADWORDS.getValue());
        q.setParameter("campaignId", campaignId);
        q.setMaxResults(1);
        
        AdWordsReportState state = null;
        try{
            state =  q.getSingleResult();
            statsLockDAO.lock(campaignId, account.getUser());
        }catch(NoResultException ex){
            return null;
        }catch(LockedException ex){
            if (state != null){
                state.setEntityId(null);
                state.setRefDate(null);
                state.setLocked(true);
            }else{
                state = new AdWordsReportState(null, null);
                state.setLocked(true);
            }
        }
        return state;
    }
    
    public DateTime getLatestRefDate(Account account , Integer entityId, EntityType entityType) {
        TypedQuery<DateTime> q = em.createQuery("SELECT s.refDate FROM Stat s WHERE " +
                                                    "s.entityType = :type " +
                                                    "AND s.entityId = :entityId ORDER BY s.refDate DESC", DateTime.class);
        
        q.setParameter("type", entityType);
        q.setParameter("entityId", entityId.longValue());
        q.setMaxResults(1);
        try{
            return q.getSingleResult();
        }catch(NoResultException ex){
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<Stat> getEntityStatsForDateRangeAndStep( Long groupId, DateTime from,
            DateTime to, EntityType entityType, StatFieldManager statFieldManager, Long campaignId, 
            Collection<Account> accounts, Provider provider, User user, Integer step) {
        
        return getEntityStatsForDateRangeAndStep(groupId, from, to, entityType, statFieldManager, 
                campaignId, accounts, provider, user, step, null, null);
    }
    
    private Integer getStep(DateTime from, DateTime to){
        return Days.daysBetween(from, to).getDays(); 
    }
    
    @SuppressWarnings("unchecked")
    public List<Stat> getEntityStatsForDateRangeAndStep( Long groupId, DateTime from,
            DateTime to, EntityType entityType, StatFieldManager statFieldManager, Long campaignId,
            Collection<Account> accounts, Provider provider, User user, Integer step, String customOrderBy, Integer limit) {
        
       //Summarize all available data to specified period
       if (step == null){
           step = getStep(from, to);
       }
       
      
       StringBuilder strBuilder = new StringBuilder("SELECT ");
       strBuilder.append(statFieldManager.getSelectQueryString(step));

       strBuilder.append(" FROM stats WHERE (ref_date >= :from AND ref_date <= :to) AND entity_type = :entityTypeName "
               + " AND provider = :provider");
       
       
       if (campaignId != null){
           strBuilder.append(" AND campaign_id = :campaignId");
       }
       if (groupId != null){
           strBuilder.append(" AND ad_group_id = :groupId");
       }
       
       if(accounts != null){
           if(provider == Provider.SKLIK){
               strBuilder.append(" AND sklik_user_id in (:accounts)");
           }else{
               strBuilder.append(" AND adwords_account_id in (:accounts)");
           }
       }
       
       if (step > 1) {
           strBuilder.append(" GROUP BY " + statFieldManager.getGroupByQueryString(step));
       } 
       
       if (!StringUtils.isEmpty(customOrderBy )){
           strBuilder.append(" ORDER BY " + customOrderBy);
       }
       //strBuilder.append(" ORDER BY name ASC");
       
       Query q = em.createNativeQuery(strBuilder.toString());
       
       if(entityType == null){
           q.setParameter("entityTypeName", EntityType.CAMPAIGN.name());
       }else{
           q.setParameter("entityTypeName", entityType.name());
       }
       
       if (campaignId != null){
           q.setParameter("campaignId", campaignId);
       } 
       if (groupId != null){
           q.setParameter("groupId", groupId);
       }  
       
       if(accounts != null){
           List<Number> accountIds = new LinkedList<>();
           Iterator<Account> accountsIterator = accounts.iterator();
           while(accountsIterator.hasNext()){
               accountIds.add(accountsIterator.next().getAccountId());
           }
           q.setParameter("accounts", accountIds);
       }
       
       q.setParameter("provider", provider.getValue());
       q.setParameter("from", from.toDate());
       q.setParameter("to", to.toDate());
       
       if(limit != null){
           q.setMaxResults(limit);
       }
    
       return mapResult(new LinkedList<StatSelectedField>(statFieldManager.getStatFields().values()), q.getResultList(), accounts);
       
    }
    
    private List<Stat> mapResult(List<StatSelectedField> fieldNames, List<Object[]> nativeResult){
        return mapResult(fieldNames, nativeResult, null);
    }
    
    private List<Stat> mapResult(List<StatSelectedField> fieldNames, List<Object[]> nativeResult, Collection<Account> accounts){
        List<Stat> resultList = new LinkedList<>();
        
        Map<Number, Account> availableAccounts = null;       
        if(accounts != null){
            availableAccounts = new HashMap<>();
            Iterator<Account> accountsIterator = accounts.iterator();
            Account currentAccount;
            while(accountsIterator.hasNext()){
                currentAccount = accountsIterator.next();
                availableAccounts.put(currentAccount.getAccountId(), currentAccount);
            }
        }
        
        Stat currentStat;
        for(int rowNr = 0 ; rowNr < nativeResult.size() ; rowNr ++){
            currentStat = new Stat();
            for(int colNr = 0 ; colNr < fieldNames.size(); colNr++){
                Object value = nativeResult.get(rowNr)[colNr];
                String fieldName = fieldNames.get(colNr).getFieldName();
                StatService.setProperty(currentStat, fieldName, value );
                
                if(accounts != null && (fieldName.equalsIgnoreCase("adwordsAccountId") 
                        || fieldName.equalsIgnoreCase("sklikUserId"))){
                    Account account = availableAccounts.get(value);
                    if(currentStat.getName() == null){
                        currentStat.setName(account.getUserName());
                    }
                    if(currentStat.getEntityId() == null){
                        currentStat.setEntityId(account.getAccountId().longValue());
                    }
                }
            }   
            resultList.add(currentStat);
        }
       return resultList;
    }
   
    
    
    @SuppressWarnings("unchecked")
    public void testQuery(){
        Query q = em.createNativeQuery("SELECT avg(avg_cpc) FROM stats");
        List<Object> res = q.getResultList();
        
        logger.info(res.get(0).toString());
        
        
    }


    /**
     * Get name of entity by its id end entity type
     * @param entityId
     * @param entityType
     * @return
     */
    public String getNameForEntityId(Long entityId, EntityType entityType) {
        if (entityId == null){
            return null;
        }
        
        TypedQuery<Stat> q = em.createQuery("SELECT s FROM Stat s WHERE s.entityId = :entityId AND s.entityType = :entityType", Stat.class);
        q.setMaxResults(1);
        q.setParameter("entityId", entityId);
        q.setParameter("entityType", entityType);
        try{
            Stat res =  q.getSingleResult();
            return res.getName();
        }catch(NoResultException ex){
            return null;
        }       
    }


    

    public List<Object[]> getBestKeywordsByCostPerConversion(Long groupId, DateTime from, DateTime to, Long campaignId,
            Provider provider, Integer limit) {
        
        Query q = em.createNativeQuery("SELECT name, sum(cost)/100/sum(conversions) as price_per_conversion, sum(conversions) FROM stats WHERE conversions IS NOT NULL AND entity_type =:entityType "
                + " AND ref_date BETWEEN :from AND :to"
                + (campaignId!=null?" AND campaign_id = :campaignId":"")
                        + (groupId!=null?" AND ad_group_id = :adGroupId":"")
                        + " GROUP BY name, floor(DATE_PART('day', ref_date - :from)/"+getStep(from, to)+")"
                        + " HAVING sum(conversions) > 0 ORDER BY price_per_conversion ASC, sum(conversions) DESC");
                
        q.setParameter("from", from.toDate());
        q.setParameter("to", to.toDate());
        q.setParameter("entityType", EntityType.KEYWORD.name());
        if(campaignId != null){
            q.setParameter("campaignId", campaignId);
        }
        if(groupId != null){
            q.setParameter("adGroupId", groupId);
        }
        q.setMaxResults(limit);
        return q.getResultList();
       
    }
    
    public List<Object[]> getBestKeywordsByConversions(Long groupId, DateTime from, DateTime to, Long campaignId,
            Provider provider, Integer limit) {
        
        Query q = em.createNativeQuery("SELECT name, sum(cost)/100/sum(conversions) as price_per_conversion, sum(cost)/100 as totalCost, sum(conversions) FROM stats WHERE conversions IS NOT NULL AND entity_type =:entityType "
                + " AND ref_date BETWEEN :from AND :to"
                + (campaignId!=null?" AND campaign_id = :campaignId":"")
                        + (groupId!=null?" AND ad_group_id = :adGroupId":"")
                        + " GROUP BY name, floor(DATE_PART('day', ref_date - :from)/"+getStep(from, to)+")"
                        + " HAVING sum(conversions) > 0 ORDER BY sum(conversions) DESC");
                
        q.setParameter("from", from.toDate());
        q.setParameter("to", to.toDate());
        q.setParameter("entityType", EntityType.KEYWORD.name());
        if(campaignId != null){
            q.setParameter("campaignId", campaignId);
        }
        if(groupId != null){
            q.setParameter("adGroupId", groupId);
        }
        q.setMaxResults(limit);
        return q.getResultList();
       
    }
    
    public long countActiveKeywords(Long groupId, DateTime from, DateTime to, Long campaignId,
            Provider provider){
        return getKeywordCountQuery(groupId, from, to, campaignId, provider, "");
    }
    
    public long countConvertedKeywords(Long groupId, DateTime from, DateTime to, Long campaignId,
            Provider provider){
        String customWhere = " AND conversions > 0";
        return getKeywordCountQuery(groupId, from, to, campaignId, provider, customWhere);
    }
    
    public long countClickedKeywords(Long groupId, DateTime from, DateTime to, Long campaignId,
            Provider provider){
        String customWhere = " AND clicks > 0";
        return getKeywordCountQuery(groupId, from, to, campaignId, provider, customWhere);

    }
    
    public double getConvertedKeywordsAveragePos(Long groupId, DateTime from, DateTime to, Long campaignId,
            Provider provider ){
        return getKeywordAgregateQuery(groupId, from, to, campaignId, provider, "avg(avg_position) as avg_avg_pos", " HAVING sum(conversions) > 0").doubleValue();
    }
    
    public double getNonConvertedKeywordsTotalCost(Long groupId, DateTime from, DateTime to, Long campaignId,
            Provider provider ){
        return getKeywordAgregateQuery(groupId, from, to, campaignId, provider, "sum(cost)/100 as total_cost", " AND entity_id not in (select distinct entity_id from stats where conversions > 0 "
                + "and entity_type = :entityType)").doubleValue();
    }
    
    public double getNonConvertedKeywordsTotalCount(Long groupId, DateTime from, DateTime to, Long campaignId,
            Provider provider ){
        return getKeywordAgregateQuery(groupId, from, to, campaignId, provider, "count(DISTINCT name) as total_count", " AND conversions = 0").longValue();
    }
    
    public long getKeywordCountQuery(Long groupId, DateTime from, DateTime to, Long campaignId,
            Provider provider, String customWhere) {
        return getKeywordAgregateQuery(groupId, from, to, campaignId, provider, "count(DISTINCT name)" , customWhere).longValue();
    }
    
    public Number getKeywordAgregateQuery(Long groupId, DateTime from, DateTime to, Long campaignId,
            Provider provider, String customFunction,  String customWhere) {
        
        Query q = em.createNativeQuery("SELECT "+customFunction+" FROM stats WHERE entity_type =:entityType "
                + " AND ref_date BETWEEN :from AND :to"
                + (campaignId!=null?" AND campaign_id = :campaignId":"")
                        + (groupId!=null?" AND ad_group_id = :adGroupId":"") +
                        customWhere);
                       
                    
                
        q.setParameter("from", from.toDate());
        q.setParameter("to", to.toDate());
        q.setParameter("entityType", EntityType.KEYWORD.name());
        if(campaignId != null){
            q.setParameter("campaignId", campaignId);
        }
        if(groupId != null){
            q.setParameter("adGroupId", groupId);
        }
        
        try{
            Number result = (Number)q.getSingleResult();
            if(result == null){
                //result exists, but is undefined
                return 0;
            }
            return result;
        }catch(NoResultException ex){
            return 0;
        }
       
    }


    public List<Object[]> getKeywordsByClickScore(Long groupId, DateTime from, DateTime to, Long campaignId, 
            Provider provider, int limit, String order) {
            Query q = em.createNativeQuery("SELECT name, sum(clicks) as clicks, sum(cost)/100 as totalCost FROM stats WHERE clicks IS NOT NULL AND entity_type =:entityType "
                    + " AND ref_date BETWEEN :from AND :to"
                    + (campaignId!=null?" AND campaign_id = :campaignId":"")
                            + (groupId!=null?" AND ad_group_id = :adGroupId":"")
                            + " GROUP BY name, floor(DATE_PART('day', ref_date - :from)/"+getStep(from, to)+")"
                            + " HAVING sum(clicks) > 0 ORDER BY sum(clicks)/(avg(avg_cpc)/100) " + order);
                    
            q.setParameter("from", from.toDate());
            q.setParameter("to", to.toDate());
            q.setParameter("entityType", EntityType.KEYWORD.name());
            if(campaignId != null){
                q.setParameter("campaignId", campaignId);
            }
            if(groupId != null){
                q.setParameter("adGroupId", groupId);
            }
            q.setMaxResults(limit);
            return q.getResultList();
    }



    public List<Object[]> getKeywordsByConvScore(Long groupId, DateTime from, DateTime to, Long campaignId,
            Provider provider, int limit, String order) {

            Query q = em.createNativeQuery("SELECT name, sum(conversions) as conversions, (sum(cost)/100)/sum(conversions) as cost_per_conversion FROM stats WHERE clicks IS NOT NULL AND entity_type =:entityType "
                    + " AND ref_date BETWEEN :from AND :to"
                    + (campaignId!=null?" AND campaign_id = :campaignId":"")
                            + (groupId!=null?" AND ad_group_id = :adGroupId":"")
                            + " GROUP BY name, floor(DATE_PART('day', ref_date - :from)/"+getStep(from, to)+")"
                            + " HAVING sum(conversions) > 0 ORDER BY sum(conversions)/(sum(cost)/100/sum(conversions)) " + order);
                    
            q.setParameter("from", from.toDate());
            q.setParameter("to", to.toDate());
            q.setParameter("entityType", EntityType.KEYWORD.name());
            if(campaignId != null){
                q.setParameter("campaignId", campaignId);
            }
            if(groupId != null){
                q.setParameter("adGroupId", groupId);
            }
            q.setMaxResults(limit);
            return q.getResultList();
    }



    public Object getNonClickedKeywordsAveragePos(Long groupId, DateTime from, DateTime to, Long campaignId,
            Provider provider) {
        return getKeywordAgregateQuery(groupId, from, to, campaignId, provider, "avg(avg_position) as avg_avg_pos, count(*) ", " HAVING sum(clicks) = 0").doubleValue();
    }

    
    
    
    
    
}
