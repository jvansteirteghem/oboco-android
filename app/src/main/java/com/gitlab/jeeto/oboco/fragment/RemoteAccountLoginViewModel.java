package com.gitlab.jeeto.oboco.fragment;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import com.gitlab.jeeto.oboco.R;
import com.gitlab.jeeto.oboco.client.AuthenticationManager;
import com.gitlab.jeeto.oboco.client.ProblemDto;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;

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
        mIsEnabledObservable.setValue(false);

        List<String> messageList = new ArrayList<String>();

        String baseUrl = mBaseUrlObservable.getValue();

        if(baseUrl == null || baseUrl.equals("")) {
            messageList.add(getMessage(R.string.action_user_log_in_name_password_error_base_url));
        } else {
            try {
                HttpUrl.get(baseUrl);
            } catch (Exception e) {
                messageList.add(getMessage(R.string.action_user_log_in_name_password_error_base_url));
            }
        }

        String name = mNameObservable.getValue();

        if(name == null || name.equals("")) {
            messageList.add(getMessage(R.string.action_user_log_in_name_password_error_name));
        }

        String password = mPasswordObservable.getValue();

        if(password == null || password.equals("")) {
            messageList.add(getMessage(R.string.action_user_log_in_name_password_error_password));
        }

        if(messageList.size() != 0) {
            String message = TextUtils.join("\n", messageList);

            mMessageObservable.setValue(message);
            mShowMessageObservable.setValue(true);

            mIsEnabledObservable.setValue(true);

            return;
        }

        baseUrl = baseUrl.replaceAll("\\/+$", "");

        mBaseUrlObservable.setValue(baseUrl);

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
                mMessageObservable.setValue(getMessage(R.string.action_user_log_in_name_password));
                mShowMessageObservable.setValue(true);

                mPasswordObservable.setValue("");

                mShowPasswordObservable.setValue(false);

                mIsEnabledObservable.setValue(true);

                mNavigateToAccountLogoutViewObservable.setValue(true);
            }

            @Override
            public void onError(Throwable e) {
                String message = null;

                ProblemDto p = getProblem(e);
                if(p != null) {
                    if(400 == p.getStatusCode()) {
                        if("PROBLEM_USER_NAME_PASSWORD_INVALID".equals(p.getCode())) {
                            message = getMessage(R.string.action_user_log_in_name_password_error);
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
