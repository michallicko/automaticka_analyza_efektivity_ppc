package cz.sortivo.reporting.model;

import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;

import cz.sortivo.sklikapi.SKlikObject;
import cz.sortivo.sklikapi.Stats;

public class StatFormatSklikImpl implements StatFormat {

    private static final long serialVersionUID = 1L;
    
    private SKlikObject entity;
    private Integer campaignId;
    private Integer adGroupId;
    private DateTime refDate;
    private Stats stats;
    private List<StatFormatSklikImpl> children = new LinkedList<>();
    
    


    public StatFormatSklikImpl(SKlikObject entity, Integer campaignId,
            Integer adGroupId, DateTime refDate, Stats stats) {
        super();
        this.entity = entity;
        this.campaignId = campaignId;
        this.adGroupId = adGroupId;
        this.refDate = refDate;
        this.stats = stats;
    }



    public Integer getCampaignId() {
        return campaignId;
    }



    public void setCampaignId(Integer campaignId) {
        this.campaignId = campaignId;
    }



    public Integer getAdGroupId() {
        return adGroupId;
    }



    public void setAdGroupId(Integer adGroupId) {
        this.adGroupId = adGroupId;
    }



    public SKlikObject getEntity() {
        return entity;
    }



    public void setEntity(SKlikObject entity) {
        this.entity = entity;
    }



    public DateTime getRefDate() {
        return refDate;
    }



    public void setRefDate(DateTime refDate) {
        this.refDate = refDate;
    }



    public Stats getStats() {
        return stats;
    }



    public void setStats(Stats stats) {
        this.stats = stats;
    }



    public List<StatFormatSklikImpl> getChildren() {
        return children;
    }



    public void setChildren(List<StatFormatSklikImpl> children) {
        this.children = children;
    }
    
    public void addChild(StatFormatSklikImpl childStats){
        this.children.add(childStats);
    }
    




    @Override
    public Object getData() {
        return this;
    }

}
