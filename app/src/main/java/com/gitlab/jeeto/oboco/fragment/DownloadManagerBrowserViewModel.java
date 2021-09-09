package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.os.Bundle;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.gitlab.jeeto.oboco.common.BaseViewModel;
import com.gitlab.jeeto.oboco.manager.DownloadWork;

public class DownloadManagerBrowserViewModel extends BaseViewModel {
    protected MutableLiveData<String> mMessageObservable;
    protected MutableLiveData<Boolean> mShowMessageObservable;
    protected MutableLiveData<DownloadWork> mSelectedDownloadWorkObservable;
    protected MutableLiveData<Boolean> mShowStartSelectedDownloadWorkDialogObservable;
    protected MutableLiveData<Boolean> mShowStopSelectedDownloadWorkDialogObservable;

    public DownloadManagerBrowserViewModel(Application application, Bundle arguments) {
        super(application, arguments);

        mMessageObservable = new MutableLiveData<String>();
        mShowMessageObservable = new MutableLiveData<Boolean>();
        mSelectedDownloadWorkObservable = new MutableLiveData<DownloadWork>();
        mShowStartSelectedDownloadWorkDialogObservable = new MutableLiveData<Boolean>();
        mShowStopSelectedDownloadWorkDialogObservable = new MutableLiveData<Boolean>();

        load();
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
    public DownloadWork getSelectedDownloadWork() {
        return mSelectedDownloadWorkObservable.getValue();
    }
    public LiveData<DownloadWork> getSelectedDownloadWorkObservable() {
        return mSelectedDownloadWorkObservable;
    }
    public void setSelectedDownloadWork(DownloadWork downloadWork) {
        mSelectedDownloadWorkObservable.setValue(downloadWork);
    }
    public Boolean getShowStartSelectedDownloadWorkDialog() {
        return mShowStartSelectedDownloadWorkDialogObservable.getValue();
    }
    public LiveData<Boolean> getShowStartSelectedDownloadWorkDialogObservable() {
        return mShowStartSelectedDownloadWorkDialogObservable;
    }
    public void setShowStartSelectedDownloadWorkDialog(Boolean startSelectedDownloadWorkDialog) {
        mShowStartSelectedDownloadWorkDialogObservable.setValue(startSelectedDownloadWorkDialog);
    }
    public Boolean getShowStopSelectedDownloadWorkDialog() {
        return mShowStopSelectedDownloadWorkDialogObservable.getValue();
    }
    public LiveData<Boolean> getShowStopSelectedDownloadWorkDialogObservable() {
        return mShowStopSelectedDownloadWorkDialogObservable;
    }
    public void setShowStopSelectedDownloadWorkDialog(Boolean stopSelectedDownloadWorkDialog) {
        mShowStopSelectedDownloadWorkDialogObservable.setValue(stopSelectedDownloadWorkDialog);
    }

    public void load() {
    }
}
