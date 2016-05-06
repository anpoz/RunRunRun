package com.playcode.runrunrun.model;

/**
 * Created by anpoz on 2016/3/21.
 */
public class MessageModel {

    /**
     * resultCode : 0
     * message : 该邮箱地址已注册过账号
     */

    private int resultCode;
    private String message;

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
}
