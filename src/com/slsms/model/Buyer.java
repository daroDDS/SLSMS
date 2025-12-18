package com.slsms.model;

public class Buyer {
    private int id;
    private String name;
    private String phone;
    private double budget;
    private String location;

    public Buyer(int id, String name, String phone, double budget, String location) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.budget = budget;
        this.location = location;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public double getBudget() { return budget; }
    public String getLocation() { return location; }

    @Override
    public String toString() { 
    	return name; 
    	}
}