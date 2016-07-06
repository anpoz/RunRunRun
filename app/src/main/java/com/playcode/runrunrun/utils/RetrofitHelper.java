package com.playcode.runrunrun.utils;

import com.google.gson.GsonBuilder;
import com.playcode.runrunrun.App;
import com.playcode.runrunrun.BuildConfig;

import java.io.File;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by anpoz on 2016/5/8.
 */
public class RetrofitHelper {
    private static final String BASE_URL = "http://codeczx.duapp.com/FitServer/";
    private Retrofit mRetrofit;
    private static RetrofitHelper INSTANCE;

    private RetrofitHelper() {
        Interceptor interceptor = chain -> {
            Request request = chain.request();
            if (!AccessUtils.isNetworkConnected(App.getContext())) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
            }

            Response response = chain.proceed(request);
            if (AccessUtils.isNetworkConnected(App.getContext())) {
                int maxAge = 60 * 60;
                response.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public,max-age=" + maxAge)
                        .build();
            } else {
                int maxStale = 60 * 60 * 24 * 28;
                response.newBuilder()
                        .removeHeader("Pragma")
                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                        .build();
            }
            return response;
        };

        File httpCacheDir = new File(App.getContext().getCacheDir(), "responses");
        Cache cache = new Cache(httpCacheDir, 1024 * 1024 * 10);//10M
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG) {
            // Log信息拦截器
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            //设置 Debug Log 模式
            builder.addInterceptor(loggingInterceptor);
        }

        OkHttpClient client = builder
                .addInterceptor(interceptor)
                .cache(cache)
                .build();


        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd HH:mm:ss.S")
                                .create()))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(client)
                .build();
    }

    public static RetrofitHelper getInstance() {
        if (INSTANCE == null) {
            synchronized (RetrofitHelper.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RetrofitHelper();
                }
            }
        }
        return INSTANCE;
    }

    public <T> T getService(Class<T> service) {
        return mRetrofit.create(service);
    }
}
