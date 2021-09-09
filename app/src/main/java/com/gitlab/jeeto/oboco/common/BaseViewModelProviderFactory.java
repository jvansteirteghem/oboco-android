package com.gitlab.jeeto.oboco.common;

import android.app.Application;
import android.os.Bundle;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class BaseViewModelProviderFactory implements ViewModelProvider.Factory {
    private Application mApplication;
    private Bundle mArguments;

    public BaseViewModelProviderFactory(Application application, Bundle arguments) {
        mApplication = application;
        mArguments = arguments;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if(BaseViewModel.class.isAssignableFrom(modelClass)) {
            try {
                return (T) modelClass.getConstructor(Application.class, Bundle.class).newInstance(mApplication, mArguments);
            } catch (Exception e) {
                throw new RuntimeException("Cannot create an instance of " + modelClass, e);
            }
        } else {
            return null;
        }
    }
}
