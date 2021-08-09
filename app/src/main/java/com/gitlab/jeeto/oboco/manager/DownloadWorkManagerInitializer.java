package com.gitlab.jeeto.oboco.manager;

import android.content.Context;
import android.util.Log;

import androidx.startup.Initializer;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import java.util.List;
import java.util.concurrent.Executors;

import static java.util.Collections.emptyList;

public class DownloadWorkManagerInitializer implements Initializer<WorkManager> {
    @Override
    public WorkManager create(Context context) {
        Configuration configuration = new Configuration.Builder()
                .setMinimumLoggingLevel(Log.ERROR)
                .setExecutor(Executors.newSingleThreadExecutor())
                .build();
        WorkManager.initialize(context, configuration);
        return WorkManager.getInstance(context);
    }

    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return emptyList();
    }
}

