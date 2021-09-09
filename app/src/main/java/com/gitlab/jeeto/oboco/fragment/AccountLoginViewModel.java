package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gitlab.jeeto.oboco.common.BaseViewModel;

public abstract class AccountLoginViewModel extends BaseViewModel {
    protected MutableLiveData<String> mBaseUrlObservable;
    protected MutableLiveData<String> mNameObservable;
    protected MutableLiveData<String> mPasswordObservable;
    protected MutableLiveData<Boolean> mShowPasswordObservable;
    protected MutableLiveData<Boolean> mIsEnabledObservable;
    protected MutableLiveData<String> mMessageObservable;
    protected MutableLiveData<Boolean> mShowMessageObservable;
    protected MutableLiveData<Boolean> mNavigateToAccountLogoutViewObservable;

    public AccountLoginViewModel(Application application, Bundle arguments) {
        super(application, arguments);

        mBaseUrlObservable = new MutableLiveData<String>();
        mNameObservable = new MutableLiveData<String>();
        mPasswordObservable = new MutableLiveData<String>();
        mShowPasswordObservable = new MutableLiveData<Boolean>();
        mIsEnabledObservable = new MutableLiveData<Boolean>();
        mMessageObservable = new MutableLiveData<String>();
        mShowMessageObservable = new MutableLiveData<Boolean>();
        mNavigateToAccountLogoutViewObservable = new MutableLiveData<Boolean>();
    }

    public String getBaseUrl() {
        return mBaseUrlObservable.getValue();
    }
    public LiveData<String> getBaseUrlObservable() {
        return mBaseUrlObservable;
    }
    public void setBaseUrl(String baseUrl) {
        mBaseUrlObservable.setValue(baseUrl);
    }
    public String getName() {
        return mNameObservable.getValue();
    }
    public LiveData<String> getNameObservable() {
        return mNameObservable;
    }
    public void setName(String name) {
        mNameObservable.setValue(name);
    }
    public String getPassword() {
        return mPasswordObservable.getValue();
    }
    public LiveData<String> getPasswordObservable() {
        return mPasswordObservable;
    }
    public void setPassword(String password) {
        mPasswordObservable.setValue(password);
    }
    public Boolean getShowPassword() {
        return mShowPasswordObservable.getValue();
    }
    public LiveData<Boolean> getShowPasswordObservable() {
        return mShowPasswordObservable;
    }
    public void setShowPassword(Boolean showPassword) {
        mShowPasswordObservable.setValue(showPassword);
    }
    public Boolean getIsEnabled() {
        return mIsEnabledObservable.getValue();
    }
    public LiveData<Boolean> getIsEnabledObservable() {
        return mIsEnabledObservable;
    }
    public String getMessage() {
        return mMessageObservable.getValue();
    }
    public LiveData<String> getMessageObservable() {
        return mMessageObservable;
    }
    public void setMessage(String message) {
        mMessageObservable.setValue(message);
    }
    public Boolean getShowMessage() {
        return mShowMessageObservable.getValue();
    }
    public LiveData<Boolean> getShowMessageObservable() {
        return mShowMessageObservable;
    }
    public void setShowMessage(Boolean showMessage) {
        mShowMessageObservable.setValue(showMessage);
    }
    public Boolean getNavigateToAccountLogoutView() {
        return mNavigateToAccountLogoutViewObservable.getValue();
    }
    public LiveData<Boolean> getNavigateToAccountLogoutViewObservable() {
        return mNavigateToAccountLogoutViewObservable;
    }
    public void setNavigateToAccountLogoutView(Boolean navigateToAccountLogoutView) {
        mNavigateToAccountLogoutViewObservable.setValue(navigateToAccountLogoutView);
    }

    public abstract void load();
    public abstract void login();
}
