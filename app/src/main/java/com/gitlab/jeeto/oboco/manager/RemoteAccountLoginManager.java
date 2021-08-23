package com.gitlab.jeeto.oboco.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.gitlab.jeeto.oboco.client.AuthenticationManager;
import com.gitlab.jeeto.oboco.fragment.AccountLoginFragment;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RemoteAccountLoginManager extends AccountLoginManager {
    private AccountLoginFragment mAccountLoginFragment;

    private SharedPreferences mSharedPreferences;

    private AuthenticationManager mAuthenticationManager;
    private Disposable mAuthenticationManagerDisposable;

    public RemoteAccountLoginManager(AccountLoginFragment fragment) {
        mAccountLoginFragment = fragment;
    }

    @Override
    public void create(Bundle savedInstanceState) {
        mSharedPreferences = mAccountLoginFragment.getContext().getSharedPreferences("application", Context.MODE_PRIVATE);

        mAuthenticationManager = new AuthenticationManager(mAccountLoginFragment.getContext());
        Observable<Throwable> observable = mAuthenticationManager.getErrors();
        observable = observable.observeOn(AndroidSchedulers.mainThread());
        observable = observable.subscribeOn(Schedulers.io());
        mAuthenticationManagerDisposable = observable.subscribe(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                mAccountLoginFragment.onError(e);
            }
        });
    }

    @Override
    public void destroy() {
        mAuthenticationManagerDisposable.dispose();
    }

    @Override
    public void saveInstanceState(Bundle outState) {

    }

    @Override
    public void load() {
        String baseUrl = mSharedPreferences.getString("baseUrl", "");
        String name = mSharedPreferences.getString("name", "");

        mAccountLoginFragment.onLoad(baseUrl, name);
    }

    @Override
    public void login(String baseUrl, String name, String password) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("baseUrl", baseUrl);
        editor.putString("name", name);
        editor.putString("password", "");
        editor.putString("accessToken", "");
        editor.putString("refreshToken", "");
        editor.commit();

        Completable completable = mAuthenticationManager.login(name, password);
        completable = completable.observeOn(AndroidSchedulers.mainThread());
        completable = completable.subscribeOn(Schedulers.io());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                mAccountLoginFragment.onLogin();
            }

            @Override
            public void onError(Throwable e) {
                mAccountLoginFragment.onError(e);
            }
        });
    }
}
