package cz.sortivo.reporting.dao;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.CaseFormat;

import cz.sortivo.reporting.annotation.StatField;
import cz.sortivo.reporting.model.Stat;
import cz.sortivo.reporting.model.StatSelectedField;

public class StatFieldManager {
    private Map<String, StatSelectedField> statFields = new LinkedHashMap<>() ;
        
    public void loadDefaultState(){
        statFields = new LinkedHashMap<>();
        
        for(Field f : Stat.class.getDeclaredFields()){
            if(!Modifier.isFinal(f.getModifiers())){
                StatField statField = f.getAnnotation(StatField.class);
                if(statField != null && !statField.ignored()){
                    StatSelectedField selectedField = new StatSelectedField();
                    
                    if(statField!= null){
                        selectedField.setFieldName(f.getName());
                        selectedField.setFunction(statField.function());
                        selectedField.setGroupBy(statField.groupBy());
                    }
                        statFields.put(f.getName(), selectedField);
                }
            }
        }
    }
    
    public String getSelectQueryString(Integer step){
        String columnName;
        
        List<String> selectedFields = new LinkedList<>();
        
        for(StatSelectedField selField: statFields.values()){
            columnName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, selField.getFieldName());
            if (step > 1 && !selField.getFunction().equalsIgnoreCase("none")) {
                selectedFields.add(selField.getFunction()+"(" + columnName + ") as " + columnName+"sel");
            } else {
                selectedFields.add(columnName);
            }       
        }
        return  StringUtils.join(selectedFields, ", ");
    }
    
    public String getGroupByQueryString(Integer step){
        List<String> groupByFields = new LinkedList<>();
        String columnName;
        
        if (step == null || step != 1){
            groupByFields.add("floor(DATE_PART('day', ref_date - :from)/"+step+")");
        }
        
        
        for(StatSelectedField selField: statFields.values()){
            columnName = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, selField.getFieldName());
            if(selField.isGroupBy()){
                groupByFields.add(columnName);
            }
        }
        return  StringUtils.join(groupByFields, ", ");
        
    }
    
    
    public Map<String, StatSelectedField> getStatFields(){
        return this.statFields;
    }
    
    
    public void setIgnored(String fieldName){
        this.setField(fieldName, true, false, null);
    }
    
    public void setField(String fieldName, boolean ignored, boolean groupBy, String agregateFunction){
        if(ignored){
            if(statFields.containsKey(fieldName)){
                statFields.remove(fieldName);
            }
            return;
        }
        
        StatSelectedField selField = new StatSelectedField();
    
        selField.setFieldName(fieldName);
        selField.setGroupBy(groupBy);
        selField.setFunction(agregateFunction);
        
        statFields.put(fieldName, selField);
    }
    

}
