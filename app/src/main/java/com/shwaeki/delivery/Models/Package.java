package com.shwaeki.delivery.Models;

public class Package {
    public int id;
    public String name;
    public int price;
    public int delivery_cost;
    public String status;
    public String deliverd_at;
    public Customer customer;
    public String SupplierID;
    public String created_at;

    public Package(int id, String name, int price, int delivery_cost, String status, String deliverd_at, String SupplierID, String created_at, Customer customer) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.delivery_cost = delivery_cost;
        this.status = status;
        this.deliverd_at = deliverd_at;
        this.customer = customer;
        this.SupplierID = SupplierID;
        this.created_at = created_at;
    }

    public Package(int id, String name, int price, int delivery_cost, String status, String deliverd_at, String SupplierID, String created_at) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.delivery_cost = delivery_cost;
        this.status = status;
        this.deliverd_at = deliverd_at;
        this.SupplierID = SupplierID;
        this.created_at = created_at;
    }
}
