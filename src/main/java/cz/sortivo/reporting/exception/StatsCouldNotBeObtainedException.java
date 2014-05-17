package cz.sortivo.reporting.exception;

public class StatsCouldNotBeObtainedException extends Exception{

    private static final long serialVersionUID = 1L;
    
    public StatsCouldNotBeObtainedException(String message, Exception ex){
        super(message, ex);
    }
    
    public StatsCouldNotBeObtainedException(String message){
        super(message);
    }
    

}
