package cz.sortivo.reporting.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import cz.sortivo.ppcgenerator.model.User;

@Entity
@Table(name = "stats_lock")
@DynamicInsert
public class StatsLock implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stats_lock_stats_lock_id_seq")
    @SequenceGenerator(name = "stats_lock_stats_lock_id_seq", sequenceName = "stats_lock_stats_lock_id_seq", allocationSize = 1)
    private Long statsLockId;
    
    private Long campaignId;
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime lockTime;
    
    
    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;


    public StatsLock(Long campaignId, User user) {
        this.campaignId = campaignId;
        this.user = user;
    }


    public Long getStatsLockId() {
        return statsLockId;
    }


    public void setStatsLockId(Long statsLockId) {
        this.statsLockId = statsLockId;
    }


    public Long getCampaignId() {
        return campaignId;
    }


    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }


    public DateTime getLockTime() {
        return lockTime;
    }


    public void setLockTime(DateTime lockTime) {
        this.lockTime = lockTime;
    }


    public User getUser() {
        return user;
    }


    public void setUser(User user) {
        this.user = user;
    }
    
    

}