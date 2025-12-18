package com.slsms.model;

public class Land {
    private int id;
    private String location;
    private double area;
    private double price;
    private String category;
    private String status;

    public Land(int id, String location, double area, double price, String category, String status) {
        this.id = id;
        this.location = location;
        this.area = area;
        this.price = price;
        this.category = category;
        this.status = status;
    }

    public int getId() { return id; }
    public String getLocation() { return location; }
    public double getArea() { return area; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
    public String getStatus() { return status; }
    
    @Override
    public String toString() {
        return location + " (" + area + "m²)";
    }
}