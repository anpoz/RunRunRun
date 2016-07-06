package com.playcode.runrunrun;

import android.app.Application;
import android.content.Context;
import android.support.v7.app.AppCompatDelegate;

import com.orm.SugarContext;

/**
 * Created by anpoz on 2016/3/16.
 */
public class App extends Application {
    private static App INSTANCE;

    public static void setServerMode(SERVER_MODE mSERVER_mode) {
        App.mSERVER_mode = mSERVER_mode;
    }

    private static SERVER_MODE mSERVER_mode = SERVER_MODE.WITHOUT_SERVER;

    public enum SERVER_MODE {
        WITHOUT_SERVER, WITH_SERVER
    }

    static {
        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        LeakCanary.install(this);
        INSTANCE = this;
        SugarContext.init(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        SugarContext.terminate();
    }

    public static Context getContext() {
        return INSTANCE;
    }

    public static SERVER_MODE getServerMode() {
        return mSERVER_mode;
    }
}
