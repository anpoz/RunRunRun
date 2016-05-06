package com.playcode.runrunrun.model;

/**
 * Created by anpoz on 2016/4/3.
 */
public class UserModel {

    /**
     * resultCode : 0
     * message : 成功获取用户信息
     * user : {"name":"陈增鑫","password":"","email":"452507871@qq.com","photo":"ghgf","weight":0,"token":"25df7f67-0afa-46e5-9b17-c9f0b3ad1dbd"}
     */

    private int resultCode;
    private String message;
    /**
     * name : 陈增鑫
     * password :
     * email : 452507871@qq.com
     * photo : ghgf
     * weight : 0
     * token : 25df7f67-0afa-46e5-9b17-c9f0b3ad1dbd
     */

    private UserEntity user;

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

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public static class UserEntity {
        private String name;
        private String password;
        private String email;
        private String photo;
        private float weight;
        private String token;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhoto() {
            return photo;
        }

        public void setPhoto(String photo) {
            this.photo = photo;
        }

        public float getWeight() {
            return weight;
        }

        public void setWeight(float weight) {
            this.weight = weight;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}
