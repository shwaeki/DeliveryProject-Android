package com.shwaeki.delivery.Models;

public class Customer {
    public int id;
    public String name;
    public String city;
    public String address;
    public String phone;
    public String latitude;
    public String longitude;


    public Customer(int id, String name, String city, String address, String phone, String latitude, String longitude) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.address = address;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
