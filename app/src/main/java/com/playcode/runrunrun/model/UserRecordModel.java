package com.playcode.runrunrun.model;

import java.util.List;

/**
 * Created by anpoz on 2016/4/3.
 */
public class UserRecordModel {

    /**
     * resultCode : 0
     * message : 查找成功
     * records : [{"email":"452507871@qq.com","date":"Mar 22, 2016 11:17:50 PM","distance":555.55,"calorie":333.33,"runTime":5000,"pointsKey":"asdlkjkl32jk4l32j4l23kj3l2k4j2l3k","address":"aslkjflaskdfjoweiruopweiru"},{"email":"452507871@qq.com","date":"Mar 23, 2016 12:34:44 AM","distance":500,"calorie":455,"runTime":333,"pointsKey":"lsdkjflsjl324","address":"shenzhen"},{"email":"452507871@qq.com","date":"Mar 23, 2016 1:34:44 AM","distance":500,"calorie":455,"runTime":333,"pointsKey":"lsdkjflsjl324","address":"shenzhen"},{"email":"452507871@qq.com","date":"Mar 23, 2016 2:34:44 AM","distance":500,"calorie":455,"runTime":333,"pointsKey":"lsdkjflsjl324","address":"shenzhen"},{"email":"452507871@qq.com","date":"Mar 24, 2016 2:34:44 AM","distance":500,"calorie":455,"runTime":333,"pointsKey":"lsdkjflsjl324","address":"shenzhen"},{"email":"452507871@qq.com","date":"Mar 25, 2016 2:34:44 AM","distance":500,"calorie":455,"runTime":333,"pointsKey":"lsdkjflsjl324","address":"shenzhen"},{"email":"452507871@qq.com","date":"Mar 26, 2016 2:34:44 AM","distance":500,"calorie":455,"runTime":333,"pointsKey":"lsdkjflsjl324","address":"shenzhen"},{"email":"452507871@qq.com","date":"Mar 26, 2016 10:34:44 AM","distance":500,"calorie":455,"runTime":333,"pointsKey":"lsdkjflsjl324","address":"shenzhen"},{"email":"452507871@qq.com","date":"Mar 26, 2016 5:34:44 PM","distance":500,"calorie":455,"runTime":333,"pointsKey":"lsdkjflsjl324","address":"shenzhen"},{"email":"452507871@qq.com","date":"Mar 27, 2016 5:34:44 PM","distance":500,"calorie":455,"runTime":333,"pointsKey":"lsdkjflsjl324","address":"shenzhen"},{"email":"452507871@qq.com","date":"Mar 28, 2016 5:34:44 PM","distance":500,"calorie":455,"runTime":333,"pointsKey":"lsdkjflsjl324","address":"shenzhen"},{"email":"452507871@qq.com","date":"Mar 29, 2016 5:34:44 PM","distance":500,"calorie":455,"runTime":333,"pointsKey":"lsdkjflsjl324","address":"shenzhen"},{"email":"452507871@qq.com","date":"Mar 29, 2016 9:34:44 PM","distance":500,"calorie":455,"runTime":333,"pointsKey":"lsdkjflsjl324","address":"shenzhen"},{"email":"452507871@qq.com","date":"Apr 3, 2016 5:16:02 PM","distance":44.1796,"calorie":3.31347,"runTime":40,"pointsKey":"3b93df4d-83b8-4139-b67a-40dccf7b6165","address":""},{"email":"452507871@qq.com","date":"Apr 3, 2016 5:25:42 PM","distance":95.7375,"calorie":7.18032,"runTime":79,"pointsKey":"885d2d56-f597-48c5-99b5-a3b9ddf2d061","address":""},{"email":"452507871@qq.com","date":"Apr 3, 2016 6:16:44 PM","distance":45.3267,"calorie":3.3995,"runTime":30,"pointsKey":"a843450e-b567-4bc6-ad9c-eadc3e631720","address":""}]
     */

    private int resultCode;
    private String message;
    /**
     * email : 452507871@qq.com
     * date : Mar 22, 2016 11:17:50 PM
     * distance : 555.55
     * calorie : 333.33
     * runTime : 5000
     * pointsKey : asdlkjkl32jk4l32j4l23kj3l2k4j2l3k
     * address : aslkjflaskdfjoweiruopweiru
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
