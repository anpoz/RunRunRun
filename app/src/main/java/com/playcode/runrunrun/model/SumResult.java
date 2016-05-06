package com.playcode.runrunrun.model;

/**
 * Created by czx on 2016/3/30.
 */
public class SumResult {
    private String resultCode;
    private String message;
    private float sumDistance;
    private float sumTime;
    public SumResult(String resultCode, String message, float sumDistance, float sumTime) {
        super();
        this.resultCode = resultCode;
        this.message = message;
        this.sumDistance = sumDistance;
        this.sumTime = sumTime;
    }
    public SumResult() {
        super();
    }
    public String getResultCode() {
        return resultCode;
    }
    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public float getSumDistance() {
        return sumDistance;
    }
    public void setSumDistance(float sumDistance) {
        this.sumDistance = sumDistance;
    }
    public float getSumTime() {
        return sumTime;
    }
    public void setSumTime(float sumTime) {
        this.sumTime = sumTime;
    }


}
