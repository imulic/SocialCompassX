package com.snipsystems.irfanmulic.socialcompassx;

/**
 * Created by irfan1 on 2/19/17.
 */
public class Person {
    String name;
    Double lon;
    Double lat;
    String place;
    String userid;

    public Person(String name, Double lat, Double lon, String place, String userid) {
        this.name = name;
        this.lon = lon;
        this.lat = lat;
        this.place = place;
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public Double getLon() {
        return lon;
    }

    public String getUserid() {return userid;}

    public void setUserid(String userid){ this.userid = userid;}

    public Double getLat() {
        return lat;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return "com.snipsystems.irfanmulic.compassone.Person{" +
                "name='" + name + '\'' +
                ", lon='" + lon + '\'' +
                ", lat='" + lat + '\'' +
                ", place='" + place + '\'' +
                '}';
    }
}
