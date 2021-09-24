package com.gitlab.jeeto.oboco.client;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.work.WorkManager;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class AuthenticationManager {
    private WorkManager mWorkManager;
    private SharedPreferences mSharedPreferences;
    private PublishSubject<Throwable> mThrowablePublishSubject = PublishSubject.create();

    public AuthenticationManager(Context context) {
        super();

        mWorkManager = WorkManager.getInstance(context.getApplicationContext());

        mSharedPreferences = context.getSharedPreferences("application", Context.MODE_PRIVATE);
    }

    public PublishSubject<Throwable> getErrors() {
        return mThrowablePublishSubject;
    }

    public String getAccessToken() {
        return mSharedPreferences.getString("accessToken", "");
    }

    public String getRefreshToken() {
        return mSharedPreferences.getString("refreshToken", "");
    }

    public Completable login(String name, String password) {
        Completable completable = new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver observer) {
                String baseUrl = mSharedPreferences.getString("baseUrl", "");

                AuthenticationService authenticationService = new AuthenticationService(baseUrl);

                UserNamePasswordDto userNamePassword = new UserNamePasswordDto();
                userNamePassword.setName(name);
                userNamePassword.setPassword(password);

                Single<UserIdDto> single = authenticationService.createUserIdByUserNamePassword(userNamePassword);
                single.subscribe(new SingleObserver<UserIdDto>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(UserIdDto userId) {
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putString("accessToken", userId.getAccessToken());
                        editor.putString("refreshToken", userId.getRefreshToken());
                        editor.commit();

                        observer.onComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mThrowablePublishSubject.onNext(e);

                        observer.onError(e);
                    }
                });
            }
        };
        return completable;
    }

    public Completable refresh() {
        Completable completable = new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver observer) {
                String baseUrl = mSharedPreferences.getString("baseUrl", "");

                AuthenticationService authenticationService = new AuthenticationService(baseUrl);

                UserTokenDto userToken = new UserTokenDto();
                userToken.setToken(getRefreshToken());

                Single<UserIdDto> single = authenticationService.createUserIdByUserToken(userToken);
                single.subscribe(new SingleObserver<UserIdDto>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(UserIdDto userId) {
                        SharedPreferences.Editor editor = mSharedPreferences.edit();
                        editor.putString("accessToken", userId.getAccessToken());
                        editor.putString("refreshToken", userId.getRefreshToken());
                        editor.commit();

                        observer.onComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(e instanceof ProblemException) {
                            ProblemException pe = (ProblemException) e;

                            if(pe.getProblem().getStatusCode() == 400) {
                                mWorkManager.cancelAllWorkByTag("download");
                                mWorkManager.pruneWork();

                                SharedPreferences.Editor editor = mSharedPreferences.edit();
                                editor.putString("accessToken", "");
                                editor.putString("refreshToken", "");
                                editor.commit();
                            }
                        }

                        mThrowablePublishSubject.onNext(e);

                        observer.onError(e);
                    }
                });
            }
        };
        return completable;
    }

    public Completable logout() {
        Completable completable = new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver observer) {
                mWorkManager.cancelAllWorkByTag("download");
                mWorkManager.pruneWork();

                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putString("accessToken", "");
                editor.putString("refreshToken", "");
                editor.commit();

                observer.onComplete();
            }
        };
        return completable;
    }
}
