package com.slsms.model;

public class Sale {
    private int saleId;
    private String landLocation;
    private String buyerName;
    private String agentName;
    private double totalAmount;
    private double paidAmount;
    private double remainingBalance;
    private String paymentMethod;
    private String date;

    public Sale(int saleId, String landLocation, String buyerName, String agentName, 
                double totalAmount, double paidAmount, double remainingBalance, 
                String paymentMethod, String date) {
        this.saleId = saleId;
        this.landLocation = landLocation;
        this.buyerName = buyerName;
        this.agentName = agentName;
        this.totalAmount = totalAmount;
        this.paidAmount = paidAmount;
        this.remainingBalance = remainingBalance;
        this.paymentMethod = paymentMethod;
        this.date = date;
    }

    public int getSaleId() { return saleId; }
    public String getLandLocation() { return landLocation; }
    public String getBuyerName() { return buyerName; }
    public String getAgentName() { return agentName; }
    public double getTotalAmount() { return totalAmount; }
    public double getPaidAmount() { return paidAmount; }
    public double getRemainingBalance() { return remainingBalance; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getDate() { return date; }
}
