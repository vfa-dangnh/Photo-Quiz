package com.haidangkf.photoquiz;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    public static MyDatabase db;
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        context = this;
        db = new MyDatabase(this);
        CreateDB();
        OpenDB();
    }

    public void CreateDB() {
        try {
            db.isCreatedDatabase();
        } catch (Exception e) {
            System.out.println("Cannot create database!");
        }
    }

    public void OpenDB() {
        try {
            db.openDatabase();
        } catch (Exception e) {
            System.out.println("Cannot open database!");
        }
    }

}
