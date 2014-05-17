package cz.sortivo.reporting.exception;

public class UnableToObtainStatsException extends Exception {

    public UnableToObtainStatsException(String message){
        super(message);
    }
    
    public UnableToObtainStatsException(String message, Throwable ex){
        super(message, ex);
    }
    
    
    
    
}
