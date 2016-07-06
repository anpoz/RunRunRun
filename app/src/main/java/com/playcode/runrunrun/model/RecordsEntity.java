package com.playcode.runrunrun.model;


import com.orm.SugarRecord;

import java.io.Serializable;

/**
 * Created by anpoz on 2016/4/2.
 */
public class RecordsEntity extends SugarRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private int _id;
    private String name;
    private String email;
    private long date;
    private float distance;
    private float calorie;
    private float runTime;
    private String pointsKey;
    private String address;
    private String pointsStr;

    public int get_id() {
        return _id;
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

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
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

    public String getPointsStr() {
        return pointsStr;
    }

    public void setPointsStr(String pointsStr) {
        this.pointsStr = pointsStr;
    }
}
