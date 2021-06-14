package com.gitlab.jeeto.oboco.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.gitlab.jeeto.oboco.api.ApplicationService;
import com.gitlab.jeeto.oboco.api.AuthenticationManager;
import com.gitlab.jeeto.oboco.api.UserDto;
import com.gitlab.jeeto.oboco.api.UserPasswordDto;
import com.gitlab.jeeto.oboco.fragment.AccountLogoutFragment;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RemoteAccountLogoutManager extends AccountLogoutManager {
    private AccountLogoutFragment mAccountLogoutFragment;

    private SharedPreferences mSharedPreferences;

    private AuthenticationManager mAuthenticationManager;
    private Disposable mAuthenticationManagerDisposable;
    private ApplicationService mApplicationService;

    public RemoteAccountLogoutManager(AccountLogoutFragment fragment) {
        mAccountLogoutFragment = fragment;
    }

    @Override
    public void create(Bundle savedInstanceState) {
        mSharedPreferences = mAccountLogoutFragment.getContext().getSharedPreferences("application", Context.MODE_PRIVATE);

        String baseUrl = mSharedPreferences.getString("baseUrl", "");

        mAuthenticationManager = new AuthenticationManager(mAccountLogoutFragment.getContext());
        Observable<Throwable> observable = mAuthenticationManager.getErrors();
        observable = observable.observeOn(AndroidSchedulers.mainThread());
        observable = observable.subscribeOn(Schedulers.io());
        mAuthenticationManagerDisposable = observable.subscribe(new Consumer<Throwable>() {
            @Override
            public void accept(Throwable e) throws Exception {
                mAccountLogoutFragment.onError(e);
            }
        });

        mApplicationService = new ApplicationService(mAccountLogoutFragment.getContext(), baseUrl, mAuthenticationManager);
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
        mAccountLogoutFragment.onLoad();
    }

    @Override
    public void logout() {
        Completable completable = mAuthenticationManager.logout();
        completable = completable.observeOn(AndroidSchedulers.mainThread());
        completable = completable.subscribeOn(Schedulers.io());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                mAccountLogoutFragment.onLogout();
            }

            @Override
            public void onError(Throwable e) {
                mAccountLogoutFragment.onError(e);
            }
        });
    }

    @Override
    public void updatePassword(String password, String updatePassword) {
        UserPasswordDto userPasswordDto = new UserPasswordDto();
        userPasswordDto.setPassword(password);
        userPasswordDto.setUpdatePassword(updatePassword);

        Single<UserDto> single = mApplicationService.updateAuthenticatedUserPassword(userPasswordDto);
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<UserDto>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(UserDto userDto) {
                mAccountLogoutFragment.onUpdatePassword();
            }

            @Override
            public void onError(Throwable e) {
                mAccountLogoutFragment.onError(e);
            }
        });
    }
}
