package com.playcode.runrunrun.model;

/**
 * Created by anpoz on 2016/3/23.
 */
public class LoginModel {

    /**
     * resultCode : 1
     * message : 登录成功
     * token : 9e88b6e3-3bf5-404a-a4cf-8009d46bed6c
     */

    private int resultCode;
    private String message;
    private String token;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
