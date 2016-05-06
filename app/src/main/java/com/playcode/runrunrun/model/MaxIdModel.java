package com.playcode.runrunrun.model;

/**
 * Created by anpoz on 2016/4/1.
 */
public class MaxIdModel {

    /**
     * resultCode : 0
     * message : 获取成功
     * maxId : 29
     */

    private int resultCode;
    private String message;
    private int id;

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
