package com.bajajallianz.myweatherapp.app;

import android.app.Application;

public class WeatherApplication extends Application {

    private static WeatherApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }

    public static WeatherApplication getInstance() {
        return instance;
    }


}
