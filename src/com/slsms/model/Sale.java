package com.slsms.model;

public class Sale {
    private int saleId;
    private String landLocation;
    private String buyerName;
    private String agentName; 
    private double price;
    private String date;

    public Sale(int saleId, String landLocation, String buyerName, String agentName, double price, String date) {
        this.saleId = saleId;
        this.landLocation = landLocation;
        this.buyerName = buyerName;
        this.agentName = agentName;
        this.price = price;
        this.date = date;
    }

    public int getSaleId() { return saleId; }
    public String getLandLocation() { return landLocation; }
    public String getBuyerName() { return buyerName; }
    public String getAgentName() { return agentName; }
    public double getPrice() { return price; }
    public String getDate() { return date; }
}