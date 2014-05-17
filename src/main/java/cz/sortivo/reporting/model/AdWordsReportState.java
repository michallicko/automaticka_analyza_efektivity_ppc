package cz.sortivo.reporting.model;

import java.util.Date;

import org.joda.time.DateTime;

public class AdWordsReportState {
    Long entityId;
    Date refDate;
    boolean locked = false;
    
    
    public AdWordsReportState( DateTime refDate, Long entityId) {
        super();
        this.entityId = entityId;
        if (refDate != null){
            this.refDate = refDate.toDate();
        }
    }
    
    
    public boolean isLocked() {
        return locked;
    }


    public void setLocked(boolean locked) {
        this.locked = locked;
    }


    public Long getEntityId() {
        return entityId;
    }
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }
    public Date getRefDate() {
        return refDate;
    }
    public void setRefDate(Date refDate) {
        this.refDate = refDate;
    }
    
    
    
    

}
