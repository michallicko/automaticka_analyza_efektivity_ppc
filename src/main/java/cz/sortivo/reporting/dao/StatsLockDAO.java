package cz.sortivo.reporting.dao;

import java.util.List;

import javax.persistence.TypedQuery;

import org.joda.time.DateTime;
import org.springframework.security.authentication.LockedException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import cz.sortivo.ppcgenerator.model.User;
import cz.sortivo.reporting.model.StatsLock;
import cz.sortivo.templates.AbstractJpaDAO;

@Repository
@Transactional
public class StatsLockDAO extends AbstractJpaDAO<StatsLock> {

    @Override
    protected Class<StatsLock> getClazz() {
        return StatsLock.class;
    }
    private static final int LOCK_EXPIRE_MINUTES = 29;
    
   
    /**
     * Unlock campaign for stats import
     * @param campaignId - locked campaign id
     * @param user - user who owns campaign
     */
    public void unlock(Long campaignId, User user){
       
        List<StatsLock> locks = getLocksByCampaignIdAndUser(campaignId, user, false);
        
        for (StatsLock lock : locks){
            this.delete(lock);
        }
        
    }
    
    public List<StatsLock> getLocksByCampaignIdAndUser(Long campaignId, User user, boolean requireActive){
        if (user == null || campaignId == null){
            throw new IllegalArgumentException("User nor campaignId cannot be null!");
        }
        
        TypedQuery<StatsLock> q = em.createQuery("SELECT s FROM StatsLock s WHERE s.campaignId = :campaignId and s.user = :user" 
        + (requireActive?" AND s.lockTime >= :lockExpireDate":""), StatsLock.class);
        q.setParameter("user", user);
        q.setParameter("campaignId", campaignId);
        if (requireActive){
            q.setParameter("lockExpireDate", new DateTime().minusMinutes(LOCK_EXPIRE_MINUTES));
        }
        List<StatsLock> locks = q.getResultList();
        return locks;
    }
    
    
    /**
     * Lock campaign for stats import
     * @param campaignId - campaign id that will be locked
     * @param user - user who owns campaign
     */
    public void lock(Long campaignId, User user) throws LockedException{
        StatsLock lock = new StatsLock(campaignId, user);
        
        List<StatsLock> locks = getLocksByCampaignIdAndUser(campaignId, user, true);
        
        if (locks.size() > 0){
            throw new LockedException("Campaign id" + campaignId + " was pereviously locked. You must call unlock before locking!");
        }
        
    }

}
