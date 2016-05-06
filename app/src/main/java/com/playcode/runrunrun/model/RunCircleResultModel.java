package com.playcode.runrunrun.model;

import java.util.List;

/**
 * Created by anpoz on 2016/4/1.
 */
public class RunCircleResultModel {

    /**
     * resultCode : 0
     * message : 获取成功
     * records : [{"id":19,"name":"陈增鑫","email":"452507871@qq.com","date":"Mar 23, 2016 1:34:44 AM","distance":500,"calorie":455,"runTime":333,"pointsKey":"lsdkjflsjl324","address":"shenzhen"},{"id":18,"name":"陈增鑫","email":"452507871@qq.com","date":"Mar 23, 2016 12:34:44 AM","distance":500,"calorie":455,"runTime":333,"pointsKey":"lsdkjflsjl324","address":"shenzhen"},{"id":7,"name":"陈增鑫","email":"452507871@qq.com","date":"Mar 22, 2016 11:17:50 PM","distance":555.55,"calorie":333.33,"runTime":5000,"pointsKey":"asdlkjkl32jk4l32j4l23kj3l2k4j2l3k","address":"aslkjflaskdfjoweiruopweiru"}]
     */

    private int resultCode;
    private String message;
    /**
     * id : 19
     * name : 陈增鑫
     * email : 452507871@qq.com
     * date : Mar 23, 2016 1:34:44 AM
     * distance : 500
     * calorie : 455
     * runTime : 333
     * pointsKey : lsdkjflsjl324
     * address : shenzhen
     */

    private List<RecordsEntity> records;

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

    public List<RecordsEntity> getRecords() {
        return records;
    }

    public void setRecords(List<RecordsEntity> records) {
        this.records = records;
    }

}
