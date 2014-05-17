package cz.sortivo.reporting.model;

public class StatSelectedField {
   
    
    private String fieldName;
    private String function = "none";
    private boolean groupBy = false;
    
    public String getFieldName() {
        return fieldName;
    }
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    public String getFunction() {
        return function;
    }
    public void setFunction(String function) {
        this.function = function;
    }
    public boolean isGroupBy() {
        return groupBy;
    }
    public void setGroupBy(boolean groupBy) {
        this.groupBy = groupBy;
    }
    
    

}
