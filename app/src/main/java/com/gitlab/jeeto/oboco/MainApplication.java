package com.gitlab.jeeto.oboco;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.startup.AppInitializer;

import com.gitlab.jeeto.oboco.client.ProblemException;
import com.gitlab.jeeto.oboco.manager.DownloadWorkManagerInitializer;

import java.io.IOException;

public class MainApplication extends Application {
    private static final String TAG = "MainApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        AppInitializer.getInstance(getApplicationContext())
                .initializeComponent(DownloadWorkManagerInitializer.class);
    }

    public static void handleError(Context context, Throwable e) {
        String message;

        if(e instanceof RuntimeException) {
            if(e.getCause() instanceof ProblemException) {
                e = e.getCause();
            } else if(e.getCause() instanceof IOException) {
                e = e.getCause();
            }
        }

        if(e instanceof ProblemException) {
            ProblemException pe = (ProblemException) e;

            if(pe.getProblem().getStatusCode() == 401) {
                SharedPreferences preferences = context.getSharedPreferences("application", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("accessToken", "");
                editor.putString("refreshToken", "");
                editor.commit();
            }

            message = "ApiError: " + pe.getProblem().getDescription() + " [" + pe.getProblem().getCode() + "].";
        } else if(e instanceof IOException) {
            message = "NetworkError: " + e.getMessage() + ".";
        } else {
            message = "Error: " + e.getMessage() + ".";
        }

        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();

        Log.v(TAG, "Error.", e);
    }
}
