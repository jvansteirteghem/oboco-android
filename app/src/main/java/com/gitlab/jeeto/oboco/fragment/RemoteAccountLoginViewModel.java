package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.gitlab.jeeto.oboco.client.AuthenticationManager;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RemoteAccountLoginViewModel extends AccountLoginViewModel {
    private static final String TAG = "AccountLogin";
    private SharedPreferences mSharedPreferences;

    private AuthenticationManager mAuthenticationManager;
    private Disposable mAuthenticationManagerDisposable;

    public RemoteAccountLoginViewModel(Application application, Bundle arguments) {
        super(application, arguments);

        mSharedPreferences = getApplication().getSharedPreferences("application", Context.MODE_PRIVATE);

        mAuthenticationManager = new AuthenticationManager(getApplication().getApplicationContext());
        Observable<Throwable> observable = mAuthenticationManager.getErrors();
        observable = observable.observeOn(AndroidSchedulers.mainThread());
        observable = observable.subscribeOn(Schedulers.io());
        mAuthenticationManagerDisposable = observable.subscribe(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                Log.v(TAG, "Error.", e);

                mMessageObservable.setValue(toMessage(e));
                mShowMessageObservable.setValue(true);
            }
        });

        load();
    }

    @Override
    protected void onCleared() {
        mAuthenticationManagerDisposable.dispose();
    }

    @Override
    public void load() {
        String baseUrl = mSharedPreferences.getString("baseUrl", "");
        mBaseUrlObservable.setValue(baseUrl);

        String name = mSharedPreferences.getString("name", "");
        mNameObservable.setValue(name);

        String password = mSharedPreferences.getString("password", "");
        mPasswordObservable.setValue(password);

        mShowPasswordObservable.setValue(false);

        mIsEnabledObservable.setValue(true);
    }

    @Override
    public void login() {
        mBaseUrlObservable.setValue(mBaseUrlObservable.getValue().replaceAll("\\/+$", ""));

        mIsEnabledObservable.setValue(false);

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("baseUrl", mBaseUrlObservable.getValue());
        editor.putString("name", mNameObservable.getValue());
        editor.putString("password", "");
        editor.putString("accessToken", "");
        editor.putString("refreshToken", "");
        editor.commit();

        Completable completable = mAuthenticationManager.login(mNameObservable.getValue(), mPasswordObservable.getValue());
        completable = completable.observeOn(AndroidSchedulers.mainThread());
        completable = completable.subscribeOn(Schedulers.io());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                mMessageObservable.setValue("You are logged in.");
                mShowMessageObservable.setValue(true);

                mPasswordObservable.setValue("");

                mShowPasswordObservable.setValue(false);

                mIsEnabledObservable.setValue(true);

                mNavigateToAccountLogoutViewObservable.setValue(true);
            }

            @Override
            public void onError(Throwable e) {
                Log.v(TAG, "Error.", e);

                mMessageObservable.setValue(toMessage(e));
                mShowMessageObservable.setValue(true);

                mIsEnabledObservable.setValue(true);
            }
        });
    }
}
