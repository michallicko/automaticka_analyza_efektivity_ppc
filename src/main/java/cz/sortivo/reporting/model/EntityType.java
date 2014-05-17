package cz.sortivo.reporting.model;

public enum EntityType {
    CAMPAIGN, ADGROUP, KEYWORD, AD;
    
    public static EntityType getInstance(String name){
        for (EntityType e : EntityType.values()){
            if(e.name().equalsIgnoreCase(name)){
                return e;
            }
        }
        throw new IllegalArgumentException("Entity type with specified name: " + name + " not found!");       
    }

}
