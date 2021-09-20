package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.client.ApplicationService;
import com.gitlab.jeeto.oboco.client.AuthenticationManager;
import com.gitlab.jeeto.oboco.client.UserDto;
import com.gitlab.jeeto.oboco.client.UserPasswordDto;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RemoteAccountLogoutViewModel extends AccountLogoutViewModel {
    private static final String TAG = "AccountLogout";
    private String mBaseUrl;

    private AuthenticationManager mAuthenticationManager;
    private Disposable mAuthenticationManagerDisposable;
    private ApplicationService mApplicationService;

    public RemoteAccountLogoutViewModel(Application application, Bundle arguments) {
        super(application, arguments);

        SharedPreferences sp = getApplication().getSharedPreferences("application", Context.MODE_PRIVATE);
        mBaseUrl = sp.getString("baseUrl", "");

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

        mApplicationService = new ApplicationService(getApplication().getApplicationContext(), mBaseUrl, mAuthenticationManager);

        load();
    }

    @Override
    protected void onCleared() {
        mAuthenticationManagerDisposable.dispose();
    }

    @Override
    public void load() {
        mPasswordObservable.setValue("");

        mUpdatePasswordObservable.setValue("");

        mShowPasswordObservable.setValue(false);

        mIsEnabledObservable.setValue(true);
    }

    @Override
    public void logout() {
        mIsEnabledObservable.setValue(false);

        Completable completable = mAuthenticationManager.logout();
        completable = completable.observeOn(AndroidSchedulers.mainThread());
        completable = completable.subscribeOn(Schedulers.io());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                mMessageObservable.setValue(getApplication().getResources().getString(R.string.account_logout_logged_out));
                mShowMessageObservable.setValue(true);

                mIsEnabledObservable.setValue(true);

                mNavigateToAccountLoginViewObservable.setValue(true);
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

    @Override
    public void updatePassword() {
        mIsEnabledObservable.setValue(false);

        UserPasswordDto userPasswordDto = new UserPasswordDto();
        userPasswordDto.setPassword(mPasswordObservable.getValue());
        userPasswordDto.setUpdatePassword(mUpdatePasswordObservable.getValue());

        Single<UserDto> single = mApplicationService.updateAuthenticatedUserPassword(userPasswordDto);
        single = single.observeOn(AndroidSchedulers.mainThread());
        single = single.subscribeOn(Schedulers.io());
        single.subscribe(new SingleObserver<UserDto>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onSuccess(UserDto userDto) {
                mMessageObservable.setValue(getApplication().getResources().getString(R.string.account_logout_updated_password));
                mShowMessageObservable.setValue(true);

                mPasswordObservable.setValue("");

                mUpdatePasswordObservable.setValue("");

                mShowPasswordObservable.setValue(false);

                mIsEnabledObservable.setValue(true);

                mNavigateToAccountLoginViewObservable.setValue(true);
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
