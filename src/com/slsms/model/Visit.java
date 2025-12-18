package com.slsms.model;

public class Visit {
    private int id;
    private String buyerName;
    private String agentName;
    private String landLocation;
    private String date;
    private String status;

    public Visit(int id, String buyerName, String agentName, String landLocation, String date, String status) {
        this.id = id;
        this.buyerName = buyerName;
        this.agentName = agentName;
        this.landLocation = landLocation;
        this.date = date;
        this.status = status;
    }
    
    public int getId() { return id; }
    public String getBuyerName() { return buyerName; }
    public String getAgentName() { return agentName; } 
    public String getLandLocation() { return landLocation; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
}