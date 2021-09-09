package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gitlab.jeeto.oboco.common.BaseViewModel;

public abstract class AccountLogoutViewModel extends BaseViewModel {
    protected MutableLiveData<String> mPasswordObservable;
    protected MutableLiveData<String> mUpdatePasswordObservable;
    protected MutableLiveData<Boolean> mShowPasswordObservable;
    protected MutableLiveData<Boolean> mIsEnabledObservable;
    protected MutableLiveData<String> mMessageObservable;
    protected MutableLiveData<Boolean> mShowMessageObservable;
    protected MutableLiveData<Boolean> mNavigateToAccountLoginViewObservable;

    public AccountLogoutViewModel(Application application, Bundle arguments) {
        super(application, arguments);

        mPasswordObservable = new MutableLiveData<String>();
        mUpdatePasswordObservable = new MutableLiveData<String>();
        mShowPasswordObservable = new MutableLiveData<Boolean>();
        mIsEnabledObservable = new MutableLiveData<Boolean>();
        mMessageObservable = new MutableLiveData<String>();
        mShowMessageObservable = new MutableLiveData<Boolean>();
        mNavigateToAccountLoginViewObservable = new MutableLiveData<Boolean>();
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
    public String getUpdatePassword() {
        return mUpdatePasswordObservable.getValue();
    }
    public LiveData<String> getUpdatePasswordObservable() {
        return mUpdatePasswordObservable;
    }
    public void setUpdatePassword(String updatePassword) {
        mUpdatePasswordObservable.setValue(updatePassword);
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
    public Boolean getNavigateToAccountLoginView() {
        return mNavigateToAccountLoginViewObservable.getValue();
    }
    public LiveData<Boolean> getNavigateToAccountLoginViewObservable() {
        return mNavigateToAccountLoginViewObservable;
    }
    public void setNavigateToAccountLoginView(Boolean navigateToAccountLoginView) {
        mNavigateToAccountLoginViewObservable.setValue(navigateToAccountLoginView);
    }

    public abstract void load();
    public abstract void logout();
    public abstract void updatePassword();
}
