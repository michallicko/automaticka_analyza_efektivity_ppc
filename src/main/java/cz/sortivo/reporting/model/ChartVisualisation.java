package cz.sortivo.reporting.model;

public class ChartVisualisation {

    private String title;
    private String data;
    private String id;
    
    
    public ChartVisualisation() {
        // TODO Auto-generated constructor stub
    }


    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }


    public String getData() {
        return data;
    }


    public void setData(String data) {
        this.data = data;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public ChartVisualisation(String title, String data, String id) {
        super();
        this.title = title;
        this.data = data;
        this.id = id;
    }

}
