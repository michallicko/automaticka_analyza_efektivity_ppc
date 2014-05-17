package cz.sortivo.reporting.model;

import java.util.Map;

import org.joda.time.DateTime;

public class StatFormatAdwordsScriptsSingleReport {
    private String name;
    private Double avgCpc;
    private Double avgCpm;
    private Double avgPageViews;
    private Double avgPosition;
    private Double avgTimeOnSite;
    private Double bounceRate;
    private Long clicks;
    private Double conversionRate;
    private Long conversions;
    private Double cost;
    private Double ctr;
    private Long impressions;
    private Double maxCpc;
    private Double topOfPgCpc;
    private Double firstPgCpc;
    private Double budget;
    private Long entityId;
    private String entityTypeName;
    private Long campaignId;
    private Long adGroupId;
    private DateTime refDate;

    private Map<String, Object> statsObj;

    
    
    public String getEntityTypeName() {
        return entityTypeName;
    }

    public void setEntityTypeName(String entityTypeName) {
        this.entityTypeName = entityTypeName;
    }

    public DateTime getRefDate() {
        return refDate;
    }

    public void setRefDate(DateTime refDate) {
        this.refDate = refDate;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public Long getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }

    public Long getAdGroupId() {
        return adGroupId;
    }

    public void setAdGroupId(Long adGroupId) {
        this.adGroupId = adGroupId;
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
        this.avgPosition = avgPosition;
    }

    public Double getAvgTimeOnSite() {
        return avgTimeOnSite;
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

    public Double getMaxCpc() {
        return maxCpc;
    }

    public void setMaxCpc(Double maxCpc) {
        this.maxCpc = maxCpc;
    }

    public Double getTopOfPgCpc() {
        return topOfPgCpc;
    }

    public void setTopOfPgCpc(Double topOfPgCpc) {
        this.topOfPgCpc = topOfPgCpc;
    }

    public Double getFirstPgCpc() {
        return firstPgCpc;
    }

    public void setFirstPgCpc(Double firstPgCpc) {
        this.firstPgCpc = firstPgCpc;
    }

    public Double getBudget() {
        return budget;
    }

    public void setBudget(Double budget) {
        this.budget = budget;
    }

    public Map<String, Object> getStatsObj() {
        return statsObj;
    }

    public void setStatsObj(Map<String, Object> statsObj) {
        this.statsObj = statsObj;
    }

}
