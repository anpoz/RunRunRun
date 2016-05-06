package com.playcode.runrunrun.utils;

import com.playcode.runrunrun.model.LoginModel;
import com.playcode.runrunrun.model.MaxIdModel;
import com.playcode.runrunrun.model.MessageModel;
import com.playcode.runrunrun.model.RunCircleResultModel;
import com.playcode.runrunrun.model.UserModel;
import com.playcode.runrunrun.model.UserRecordModel;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by anpoz on 2016/4/4.
 */
public interface APIUtils {
    @GET("RunRecordServlet?method=findmaxid")
    Observable<MaxIdModel> getMaxId();

    @GET("UserServlet?method=getUser")
    Observable<UserModel> getUser(@Query("token") String token);

    @GET("UserServlet?method=login")
    Observable<LoginModel> userLogin(@Query("password") String password,
                                     @Query("email") String email);

    @GET("RunRecordServlet?method=findall")
    Observable<UserRecordModel> getUserRecords(@Query("token") String token);

    @GET("UserServlet?method=register")
    Observable<MessageModel> userReg(@Query("name") String username,
                                     @Query("password") String password,
                                     @Query("email") String email);

    @GET("RunRecordServlet?method=getrecord")
    Observable<RunCircleResultModel> getRecordsById(@Query("id") int id,
                                                    @Query("token") String token);

    @GET("UserServlet?method=setPhoto")
    Observable<MessageModel> updatePhotoInfo(@Query("photo") String photoKey, @Query("token") String tokn);

    @GET("UserServlet?method=setWeight")
    Observable<MessageModel> setWeigth(@Query("token") String token, @Query("weight") float weight);

    @FormUrlEncoded
    @POST("RunRecordServlet")
    Observable<MessageModel> add(@Field("method") String method, @Field("token") String token,
                                 @Field("date") String date, @Field("distance") String distance,
                                 @Field("calorie") String calorie, @Field("runtime") String runTime,
                                 @Field("pointskey") String pointskey, @Field("address") String address);
}
