package com.slsms.model;

public class Buyer {
    private int id;
    private String name;
    private String phone;
    private double budget;
    private String location;
    private String agentName; 

    // Constructor for Admin View
    public Buyer(int id, String name, String phone, double budget, String location, String agentName) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.budget = budget;
        this.location = location;
        this.agentName = agentName;
    }
    
    // Constructor for Agent View
    public Buyer(int id, String name, String phone, double budget, String location) {
        this(id, name, phone, budget, location, "");
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public double getBudget() { return budget; }
    public String getLocation() { return location; }
    public String getAgentName() { return agentName; }
    @Override
    public String toString() { return name; }
}