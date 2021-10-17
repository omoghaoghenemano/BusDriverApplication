package com.bustracking.myschool.bustracker.Models;



public class Driver
{
    Driver()
    {}

    public String name,email,password,vehiclenumber;
    public Double lat,lng;

    public Driver(String name, String email, String password, String vehiclenumber,Double lat, Double lng) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.vehiclenumber = vehiclenumber;
        this.lat = lat;
        this.lng = lng;
    }
}
