package com.slsms.model;

public class Sale {
    private int saleId;
    private String landLocation;
    private String buyerName;
    private double price;
    private String date;

    public Sale(int saleId, String landLocation, String buyerName, double price, String date) {
        this.saleId = saleId;
        this.landLocation = landLocation;
        this.buyerName = buyerName;
        this.price = price;
        this.date = date;
    }

    public int getSaleId() { return saleId; }
    public String getLandLocation() { return landLocation; }
    public String getBuyerName() { return buyerName; }
    public double getPrice() { return price; }
    public String getDate() { return date; }
}