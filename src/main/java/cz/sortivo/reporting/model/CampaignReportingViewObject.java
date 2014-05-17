package cz.sortivo.reporting.model;

import cz.sortivo.ppcgenerator.model.Provider;

public class CampaignReportingViewObject {
    private String name;
    private Long campaignId;
    private Provider provider;
    
    public CampaignReportingViewObject(){
        
    }
    
    public CampaignReportingViewObject(String name, Long campaignId, Integer providerCode){
        this.name = name;
        this.campaignId = campaignId;
        this.provider = Provider.getInstanceByValue(providerCode);
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Long getCampaignId() {
        return campaignId;
    }
    public void setCampaignId(Long campaignId) {
        this.campaignId = campaignId;
    }
    public Provider getProvider() {
        return provider;
    }
    public void setProvider(Provider provider) {
        this.provider = provider;
    }
    
    

}
