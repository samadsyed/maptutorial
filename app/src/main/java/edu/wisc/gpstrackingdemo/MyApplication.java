package edu.wisc.gpstrackingdemo;

import android.app.Application;

import java.util.ArrayList;

public class MyApplication extends Application
{
    private static MyApplication singleton;

    private List<Location> myLocations;

    private List<Location> getMyLocations()
    {
        return myLocations;
    }

    public MyApplication getInstance()
    {
        return singleton;
    }

    public void onCreate()
    {
        super.onCreate();
        singleton = this;
        myLocations = new ArrayList<>();
    }
}
