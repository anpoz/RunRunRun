package com.playcode.runrunrun.model;


import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by anpoz on 2016/4/2.
 */
public class RecordsEntity implements Serializable {
    private int id;
    private String name;
    private String email;
    private Timestamp date;
    private float distance;
    private float calorie;
    private float runTime;
    private String pointsKey;
    private String address;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float getCalorie() {
        return calorie;
    }

    public void setCalorie(float calorie) {
        this.calorie = calorie;
    }

    public float getRunTime() {
        return runTime;
    }

    public void setRunTime(float runTime) {
        this.runTime = runTime;
    }

    public String getPointsKey() {
        return pointsKey;
    }

    public void setPointsKey(String pointsKey) {
        this.pointsKey = pointsKey;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
