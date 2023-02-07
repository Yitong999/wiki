package cpen221.mp3.server;

public class WikiRequest {

    //RI: id cannot be null
    //AF: Represents a function in the wikimediator with parameters
    //All variables not in function are in null

    private String id;
    private String type;
    private String query;
    private String pageTitle;
    private Integer limit;
    private Integer timeout;
    private Integer maxItems;
    private Integer timeLimitInSeconds;
    private Integer timeWindowInSeconds;
    private String pageTitle1;
    private String pageTitle2;

    public String getId(){ return id; }

    public String getType(){ return type; }

    public String getQuery(){ return query; }

    public Integer getLimit() { return limit; }

    public Integer getTimeOut() { return timeout; }

    public String getPageTitle(){ return pageTitle; }

    public Integer getMaxItems() { return maxItems; }

    public Integer getTimeLimitInSeconds() { return timeLimitInSeconds; }

    public Integer getTimeWindowInSeconds() { return timeWindowInSeconds; }

    public String getPageTitle1(){ return pageTitle1; }

    public String getPageTitle2(){ return pageTitle2; }
}
