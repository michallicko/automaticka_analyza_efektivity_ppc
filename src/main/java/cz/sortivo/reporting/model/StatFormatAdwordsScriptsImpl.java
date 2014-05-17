package cz.sortivo.reporting.model;

import java.util.List;


public class StatFormatAdwordsScriptsImpl implements StatFormat {

    private static final long serialVersionUID = 1L;

    List<StatFormatAdwordsScriptsSingleReport> stats;

    public StatFormatAdwordsScriptsImpl(List<StatFormatAdwordsScriptsSingleReport> stats){
        this.stats = stats;
    }
    
    @Override
    public Object getData() {
        return stats;
    }
}
