package com.gitlab.jeeto.oboco.api;

import android.content.Context;
import android.content.SharedPreferences;

import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

public class AuthenticationManager {
    private SharedPreferences sp;
    private PublishSubject<Throwable> subject = PublishSubject.create();

    public AuthenticationManager(Context context) {
        super();

        sp = context.getSharedPreferences("application", Context.MODE_PRIVATE);
    }

    public PublishSubject<Throwable> getErrors() {
        return subject;
    }

    public String getAccessToken() {
        return sp.getString("accessToken", "");
    }

    public String getRefreshToken() {
        return sp.getString("refreshToken", "");
    }

    public Completable login(String name, String password) {
        Completable completable = new Completable() {
            @Override
            protected void subscribeActual(CompletableObserver observer) {
                String baseUrl = sp.getString("baseUrl", "");

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
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("accessToken", userId.getAccessToken());
                        editor.putString("refreshToken", userId.getRefreshToken());
                        editor.commit();

                        observer.onComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        subject.onNext(e);

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
                String baseUrl = sp.getString("baseUrl", "");

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
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("accessToken", userId.getAccessToken());
                        editor.putString("refreshToken", userId.getRefreshToken());
                        editor.commit();

                        observer.onComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        subject.onNext(e);

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
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("accessToken", "");
                editor.putString("refreshToken", "");
                editor.commit();

                observer.onComplete();
            }
        };
        return completable;
    }
}
