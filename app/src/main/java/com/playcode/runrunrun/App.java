package com.playcode.runrunrun;

import android.app.Application;
import android.content.Context;
import android.support.v7.app.AppCompatDelegate;

import com.facebook.stetho.Stetho;

/**
 * Created by anpoz on 2016/3/16.
 */
public class App extends Application {
    private static App INSTANCE;

    static {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG)
            Stetho.initializeWithDefaults(this);
//        LeakCanary.install(this);
        INSTANCE = this;
    }

    public static Context getContext() {
        return INSTANCE;
    }
}
