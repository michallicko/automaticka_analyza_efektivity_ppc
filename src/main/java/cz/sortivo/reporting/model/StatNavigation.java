package cz.sortivo.reporting.model;

public class StatNavigation {
    String elementName;
    String actionName;
    String levelName;
    
    public StatNavigation(String elementName, String actionName) {
        this.elementName = elementName;
        this.actionName=  actionName;
    }
    public StatNavigation(String elementName, String actionName, String levelName) {
        this.elementName = elementName;
        this.actionName=  actionName;
        this.levelName = levelName;
    }
    
    public String getElementName() {
        return elementName;
    }
    public void setElementName(String elementName) {
        this.elementName = elementName;
    }
    public String getActionName() {
        return actionName;
    }
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    public String getLevelName() {
        return levelName;
    }
    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }
    
    

}
