package com.playcode.runrunrun;

import android.app.Application;
import android.support.v7.app.AppCompatDelegate;

import com.facebook.stetho.Stetho;

/**
 * Created by anpoz on 2016/3/16.
 */
public class App extends Application {
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
    }
}
