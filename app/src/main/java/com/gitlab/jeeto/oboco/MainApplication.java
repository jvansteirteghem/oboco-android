package com.gitlab.jeeto.oboco;

import android.app.Application;

import androidx.startup.AppInitializer;

import com.gitlab.jeeto.oboco.manager.DownloadWorkManagerInitializer;

public class MainApplication extends Application {
    private static final String TAG = "MainApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        AppInitializer.getInstance(getApplicationContext())
                .initializeComponent(DownloadWorkManagerInitializer.class);
    }
}
