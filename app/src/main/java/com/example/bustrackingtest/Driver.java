package com.example.bustrackingtest;


public class Driver {

    String busName;
    String route;
    String time;

    public Driver(String busName, String route, String time){
        this.busName = busName;
        this.route = route;
        this.time = time;
    }

    public String getBusName(){
        return busName;
    }

    public String getRouteName(){
        return route;
    }

    public String getTime(){
        return time;
    }
}