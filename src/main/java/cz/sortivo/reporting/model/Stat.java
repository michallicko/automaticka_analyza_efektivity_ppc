package cz.sortivo.reporting.model;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import cz.sortivo.ppcgenerator.model.Account;
import cz.sortivo.ppcgenerator.model.User;
import cz.sortivo.reporting.annotation.StatField;

/**
 * Stat class represents 1 statistics record for specified date
 * 
 * @author michal
 * 
 */
@Entity
@Table(name = "stats")
@DynamicInsert
public class Stat implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int ENTITY_TYPE_CAMPAIGN = 1;
    public static final int ENTITY_TYPE_GROUP = 2;
    public static final int ENTITY_TYPE_KEYWORD = 3;
    public static final int ENTITY_TYPE_AD = 4;
           
    // !unique entity id, references foreign provider ids
    @StatField(function="none", groupBy = true)
    private Long entityId;

    @StatField(ignored=true)
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime" )
    private DateTime refDate;

    @StatField(ignored=true)
    @Enumerated(EnumType.STRING)
    private EntityType entityType;

    @StatField(function="none", groupBy=true)
    private String name;
    @StatField(function="avg")
    private Double avgCpc;
    @StatField(function="avg")
    private Double avgCpm;
    @StatField(function="avg")
    private Double avgPageViews;
    @StatField(function="avg")
    private Double avgPosition;
    @StatField(function="avg")
    private Double avgTimeOnSite;
    @StatField(function="avg")
    private Double bounceRate;
    @StatField(function="sum")
    private Long clicks;
    @StatField(function="avg")
    private Double conversionRate;
    @StatField(function="sum")
    private Long conversions;
    @StatField(function="sum")
    private Double cost;
    @StatField(function="avg")
    private Double ctr;
    @StatField(function="sum")
    private Long impressions;
    @StatField(function="avg")
    private Double maxCpc;
    @StatField(function="avg")
    private Double topOfPageCpc;
    @StatField(function="avg")
    private Double firstPageCpc;
    @StatField(function="sum")
    private Double budget;
    @StatField(ignored=true)
    private Integer provider;
    @StatField(function="none", groupBy = true)
    private Long adGroupId;
    @StatField(ignored = true)
    private Long campaignId;
    
    @Transient
    @StatField(ignored=true)
    private Double conversionCost;
    
    @Transient
    @StatField(ignored=true)
    private Account account;
    
    @StatField(function="none", groupBy = true)
    private Integer sklikUserId;

    @StatField(ignored=true)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @StatField(ignored=true)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stats_stats_id_seq")
    @SequenceGenerator(name = "stats_stats_id_seq", sequenceName = "stats_stats_id_seq", allocationSize = 1)
    @Column(name = "stat_id")
    private Long id;

   
    
    
    
    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Integer getSklikUserId() {
        return sklikUserId;
    }

    public void setSklikUserId(Integer sklikUserId) {
        this.sklikUserId = sklikUserId;
    }

    public Stat() {


    }

    public Long getAdGroupId() {
        return adGroupId;
    }

    public void setAdGroupId(Long adGroupId) {
        this.adGroupId = adGroupId;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public Stat(StatFormatAdwordsScriptsSingleReport singleReport) {

    }

    /**
     * @return the user
     */
    public User getUser() {
        return user;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(User user) {
        this.user = user;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public DateTime getRefDate() {
        return refDate;
    }

    public void setRefDate(DateTime refDate) {
        this.refDate = refDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAvgCpc() {
        return avgCpc;
    }

    public void setAvgCpc(Double avgCpc) {
        this.avgCpc = avgCpc;
    }

    public Double getAvgCpm() {
        return avgCpm;
    }

    public void setAvgCpm(Double avgCpm) {
        this.avgCpm = avgCpm;
    }

    public Double getAvgPageViews() {
        return avgPageViews;
    }

    public void setAvgPageViews(Double avgPageViews) {
        this.avgPageViews = avgPageViews;
    }

    public Double getAvgPosition() {
        return avgPosition;
    }

    public void setAvgPosition(Double avgPosition) {
        if(avgPosition != null && avgPosition.equals(0D)){
            this.avgPosition = null;
        }else{
            this.avgPosition = avgPosition;
        }
    }

    public Double getAvgTimeOnSite() {
        return avgTimeOnSite;
    }

    public Double getMaxCpc() {
        return maxCpc;
    }

    public void setMaxCpc(Double maxCpc) {
        this.maxCpc = maxCpc;
    }

    public Double getTopOfPgCpc() {
        return topOfPageCpc;
    }

    public void setTopOfPgCpc(Double topOfPgCpc) {
        this.topOfPageCpc = topOfPgCpc;
    }

    public Double getFirstPgCpc() {
        return firstPageCpc;
    }

    public void setFirstPgCpc(Double firstPgCpc) {
        this.firstPageCpc = firstPgCpc;
    }


    public void setAvgTimeOnSite(Double avgTimeOnSite) {
        this.avgTimeOnSite = avgTimeOnSite;
    }

    public Double getBounceRate() {
        return bounceRate;
    }

    public void setBounceRate(Double bounceRate) {
        this.bounceRate = bounceRate;
    }

    public Long getClicks() {
        return clicks;
    }

    public void setClicks(Long clicks) {
        this.clicks = clicks;
    }

    public Double getConversionRate() {
        return conversionRate;
    }

    public void setConversionRate(Double conversionRate) {
        this.conversionRate = conversionRate;
    }

    public Long getConversions() {
        return conversions;
    }

    public void setConversions(Long conversions) {
        this.conversions = conversions;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Double getCtr() {
        return ctr;
    }

    public void setCtr(Double ctr) {
        this.ctr = ctr;
    }

    public Long getImpressions() {
        return impressions;
    }

    public void setImpressions(Long impressions) {
        this.impressions = impressions;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public Integer getProvider() {
        return provider;
    }

    public void setProvider(Integer provider) {
        this.provider = provider;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long stat_id) {
        this.id = stat_id;
    }
};
