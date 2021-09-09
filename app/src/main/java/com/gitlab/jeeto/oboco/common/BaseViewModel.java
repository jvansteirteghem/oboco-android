package com.gitlab.jeeto.oboco.common;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.AndroidViewModel;

import com.gitlab.jeeto.oboco.client.ProblemException;

import java.io.IOException;

public class BaseViewModel extends AndroidViewModel {
    private Bundle mArguments;

    public BaseViewModel(Application application, Bundle arguments) {
        super(application);

        mArguments = arguments;
    }

    public Bundle getArguments() {
        return mArguments;
    }
    public static String toMessage(Throwable e) {
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
            message = "ApiError: " + pe.getProblem().getDescription() + " [" + pe.getProblem().getCode() + "].";
        } else if(e instanceof IOException) {
            message = "NetworkError: " + e.getMessage() + ".";
        } else {
            message = "Error: " + e.getMessage() + ".";
        }

        return message;
    }
}
