package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.client.ApplicationService;
import com.gitlab.jeeto.oboco.client.AuthenticationManager;
import com.gitlab.jeeto.oboco.client.ProblemDto;
import com.gitlab.jeeto.oboco.client.UserDto;
import com.gitlab.jeeto.oboco.client.UserPasswordDto;

import java.util.ArrayList;
import java.util.List;

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
                String message = null;

                ProblemDto p = getProblem(e);
                if(p != null) {
                    if(400 == p.getStatusCode()) {
                        if("PROBLEM_USER_TOKEN_INVALID".equals(p.getCode())) {
                            message = getMessage(R.string.action_user_log_in_token_error);
                        }
                    }
                }

                if(message == null) {
                    message = getMessage(e);
                }

                mMessageObservable.setValue(message);
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
                mMessageObservable.setValue(getMessage(R.string.action_user_log_out));
                mShowMessageObservable.setValue(true);

                mIsEnabledObservable.setValue(true);

                mNavigateToAccountLoginViewObservable.setValue(true);
            }

            @Override
            public void onError(Throwable e) {
                mMessageObservable.setValue(getMessage(e));
                mShowMessageObservable.setValue(true);

                mIsEnabledObservable.setValue(true);
            }
        });
    }

    @Override
    public void updatePassword() {
        mIsEnabledObservable.setValue(false);

        List<String> messageList = new ArrayList<String>();

        String password = mPasswordObservable.getValue();

        if(password == null || password.equals("")) {
            messageList.add(getMessage(R.string.action_user_update_password_error_password));
        }

        String updatePassword = mUpdatePasswordObservable.getValue();

        if(updatePassword == null || updatePassword.equals("")) {
            messageList.add(getMessage(R.string.action_user_update_password_error_update_password));
        }

        if(messageList.size() != 0) {
            String message = TextUtils.join("\n", messageList);

            mMessageObservable.setValue(message);
            mShowMessageObservable.setValue(true);

            mIsEnabledObservable.setValue(true);

            return;
        }

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
                mMessageObservable.setValue(getMessage(R.string.action_user_update_password));
                mShowMessageObservable.setValue(true);

                mPasswordObservable.setValue("");

                mUpdatePasswordObservable.setValue("");

                mShowPasswordObservable.setValue(false);

                mIsEnabledObservable.setValue(true);

                mNavigateToAccountLoginViewObservable.setValue(true);
            }

            @Override
            public void onError(Throwable e) {
                String message = null;

                ProblemDto p = getProblem(e);
                if(p != null) {
                    if(400 == p.getStatusCode()) {
                        if("PROBLEM_USER_PASSWORD_INVALID".equals(p.getCode())) {
                            message = getMessage(R.string.action_user_update_password_error);
                        }
                    }
                }

                if(message == null) {
                    message = getMessage(e);
                }

                mMessageObservable.setValue(message);
                mShowMessageObservable.setValue(true);

                mIsEnabledObservable.setValue(true);
            }
        });
    }
}
